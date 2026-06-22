package com.rafel.bgt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(val version: String, val downloadUrl: String, val releaseNotes: String = "")

object UpdateChecker {

    private const val API_URL =
        "https://api.github.com/repos/Carchofo/BGT/releases/latest"

    suspend fun check(currentVersion: String): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(URL(API_URL).readText())
                val tag = json.getString("tag_name").removePrefix("v")
                if (!isNewer(tag, currentVersion)) return@withContext null

                val assets = json.getJSONArray("assets")
                val url = (0 until assets.length())
                    .map { assets.getJSONObject(it) }
                    .firstOrNull { it.getString("name").endsWith(".apk") }
                    ?.getString("browser_download_url")
                    ?: return@withContext null

                val notes = runCatching { json.getString("body") }.getOrDefault("")
                UpdateInfo(tag, url, notes)
            } catch (e: Exception) {
                null
            }
        }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").mapNotNull { it.toIntOrNull() }
        val l = local.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(r.size, l.size)) {
            if (r.getOrElse(i) { 0 } != l.getOrElse(i) { 0 })
                return r.getOrElse(i) { 0 } > l.getOrElse(i) { 0 }
        }
        return false
    }
}
