package com.rafel.bgt

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object BugReporter {
    // LAN webhook — works on local network; swap for tunnel URL when exposing externally
    private const val WEBHOOK_URL = "http://192.168.0.25:5678/webhook/bgt-bug"
    private const val FALLBACK_EMAIL = "rafel@spicyoffers.com"

    suspend fun send(ctx: Context, game: String, message: String): Boolean {
        val pkg = try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) { null }

        val body = JSONObject().apply {
            put("game", game)
            put("message", message)
            put("version", pkg?.versionName ?: "unknown")
            put("versionCode", pkg?.longVersionCode ?: 0)
            put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("sdk", Build.VERSION.SDK_INT)
            put("ts", System.currentTimeMillis())
        }.toString()

        val sent = withContext(Dispatchers.IO) {
            runCatching {
                (URL(WEBHOOK_URL).openConnection() as HttpURLConnection).run {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 6_000
                    readTimeout = 6_000
                    setRequestProperty("Content-Type", "application/json")
                    outputStream.use { it.write(body.toByteArray()) }
                    responseCode in 200..299
                }
            }.getOrDefault(false)
        }

        if (!sent) sendEmail(ctx, game, message, pkg?.versionName ?: "?")
        return sent
    }

    private fun sendEmail(ctx: Context, game: String, message: String, version: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$FALLBACK_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, "[BGT Bug] $game v$version")
            putExtra(
                Intent.EXTRA_TEXT,
                "Juego: $game\nVersionn: $version\nDispositivo: ${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})\n\n$message"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(intent) }
    }
}
