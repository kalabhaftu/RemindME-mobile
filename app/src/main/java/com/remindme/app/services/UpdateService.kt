package com.remindme.app.services

import android.content.pm.PackageManager
import android.util.Log
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val updateAvailable: Boolean
)

object UpdateService {
    private const val REPO = "kalabhaftu/RemindME-releases"
    private const val API_URL = "https://api.github.com/repos/$REPO/releases/latest"
    private const val TAG = "UpdateService"

    fun checkForUpdate(packageManager: PackageManager, packageName: String): UpdateInfo? {
        return try {
            val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: return null

            val response = URL(API_URL).readText()
            val data = JSONObject(response)

            val tagName = data.optString("tag_name", "").removePrefix("v")
            val body = data.optString("body", "")

            val assets = data.optJSONArray("assets") ?: return null
            var downloadUrl = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.optString("name").endsWith(".apk")) {
                    downloadUrl = asset.optString("browser_download_url", "")
                    break
                }
            }

            val hasUpdate = isNewer(tagName, currentVersion)

            UpdateInfo(
                latestVersion = tagName,
                currentVersion = currentVersion,
                downloadUrl = downloadUrl,
                releaseNotes = body,
                updateAvailable = hasUpdate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Update check failed", e)
            null
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }.toMutableList()
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }.toMutableList()

        while (latestParts.size < 3) latestParts.add(0)
        while (currentParts.size < 3) currentParts.add(0)

        for (i in 0 until 3) {
            when {
                latestParts[i] > currentParts[i] -> return true
                latestParts[i] < currentParts[i] -> return false
            }
        }
        return false
    }
}
