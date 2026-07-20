package com.remindme.app.services

import com.remindme.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class LogoResolution(val domain: String, val logoUrl: String)

object LogoResolver {
    suspend fun resolve(query: String): LogoResolution? = withContext(Dispatchers.IO) {
        val apiBase = BuildConfig.WEB_API_URL.trim().trimEnd('/')
        if (apiBase.isNotBlank()) {
            runCatching {
                val connection = (URL("$apiBase/api/logo-resolve").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 5_000
                    readTimeout = 8_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
                connection.outputStream.use { it.write(JSONObject().put("query", query).toString().toByteArray()) }
                if (connection.responseCode in 200..299) {
                    val data = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                    val domain = data.optString("domain")
                    val logoUrl = data.optString("logoUrl")
                    if (domain.isNotBlank() && logoUrl.isNotBlank()) return@runCatching LogoResolution(domain, logoUrl)
                }
                connection.disconnect()
                null
            }.getOrNull()?.let { return@withContext it }
        }

        val domain = query.lowercase()
            .replace(Regex("^https?://"), "")
            .substringBefore('/')
            .removePrefix("www.")
            .ifBlank { return@withContext null }
            .let { if (it.contains('.')) it else "$it.com" }
        LogoResolution(domain, "https://logo.clearbit.com/$domain")
    }
}
