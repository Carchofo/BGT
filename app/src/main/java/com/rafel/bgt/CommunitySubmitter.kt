package com.rafel.bgt

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object CommunitySubmitter {
    // LAN webhook — works on local network; swap for tunnel URL when exposing externally
    private const val WEBHOOK_URL = "http://192.168.0.25:5678/webhook/bgt-community-submit"
    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 80

    enum class SubmissionType(val wire: String) {
        PHOTO("photo"),          // foto de cartas/componentes de un juego
        FEEDBACK("feedback"),    // comentario/sugerencia sobre la app
        GAME_PROPOSAL("game")    // propuesta de juego nuevo
    }

    suspend fun send(
        ctx: Context,
        game: String,
        type: SubmissionType,
        message: String,
        imageUri: Uri? = null
    ): Boolean {
        val pkg = try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) { null }

        val imageB64 = imageUri?.let { withContext(Dispatchers.IO) { encodeImage(ctx, it) } }

        val body = JSONObject().apply {
            put("game", game)
            put("type", type.wire)
            put("message", message)
            put("version", pkg?.versionName ?: "unknown")
            put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("sdk", Build.VERSION.SDK_INT)
            put("ts", System.currentTimeMillis())
            if (imageB64 != null) put("image", imageB64)
        }.toString()

        return withContext(Dispatchers.IO) {
            runCatching {
                (URL(WEBHOOK_URL).openConnection() as HttpURLConnection).run {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("Content-Type", "application/json")
                    outputStream.use { it.write(body.toByteArray()) }
                    responseCode in 200..299
                }
            }.getOrDefault(false)
        }
    }

    /** Reescala a máx 1600px y comprime a JPEG 80 para no saturar el webhook. */
    private fun encodeImage(ctx: Context, uri: Uri): String? = runCatching {
        val original = ctx.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: return null

        val scale = MAX_DIMENSION.toFloat() / maxOf(original.width, original.height)
        val bmp = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true
            )
        } else original

        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()
}
