package com.rafel.bgt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateChecker {

    private const val API_URL =
        "https://api.github.com/repos/Carchofo/BGT/releases/latest"

    suspend fun checkForUpdate(currentVersion: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val json = URL(API_URL).readText()
                val tag = JSONObject(json).getString("tag_name")
                    .removePrefix("v")
                if (isNewer(tag, currentVersion)) tag else null
            } catch (e: Exception) {
                null
            }
        }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").mapNotNull { it.toIntOrNull() }
        val l = local.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(r.size, l.size)) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv != lv) return rv > lv
        }
        return false
    }
}
