package com.bridge.yandexbyd

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    private const val GITHUB_API =
        "https://api.github.com/repos/scroodge/VoltFlow-Nav/releases/latest"
    private const val PREFS_NAME = "update_prefs"
    private const val KEY_LAST_CHECK = "last_check"
    private const val KEY_AUTO_CHECK = "auto_check_enabled"
    private const val CHECK_INTERVAL_MS = 10 * 60 * 1000L

    data class UpdateInfo(
        val version: String,
        val downloadUrl: String,
        val releaseNotes: String,
    )

    fun isAutoCheckEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_CHECK, true)

    fun setAutoCheckEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTO_CHECK, enabled)
            .apply()
    }

    suspend fun checkForUpdate(context: Context, forceCheck: Boolean = false): UpdateInfo? =
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
            val now = System.currentTimeMillis()

            if (!forceCheck && now - lastCheck < CHECK_INTERVAL_MS) {
                return@withContext null
            }

            prefs.edit().putLong(KEY_LAST_CHECK, now).apply()

            val body = fetchGitHubRelease()
            ReleaseJsonParser.parseReleaseJson(body, getAppVersion(context))
        }

    suspend fun downloadAndInstall(
        context: Context,
        update: UpdateInfo,
        onProgress: (String) -> Unit = {},
    ) = withContext(Dispatchers.IO) {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val fileName = apkFileName(update.version)
        val destFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName,
        )
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse(update.downloadUrl))
            .setTitle("VoltFlow Nav ${update.version}")
            .setDescription("VoltFlow Nav update")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadId = downloadManager.enqueue(request)
        onProgress("Downloading: 0%")

        var finished = false
        while (!finished) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS),
                )
                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                        )
                        val downloaded = cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR,
                            ),
                        )
                        if (total > 0) {
                            val pct = (downloaded * 100 / total).toInt()
                            onProgress("Downloading: $pct%")
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finished = true
                        onProgress("Downloaded. Installing...")
                    }
                    DownloadManager.STATUS_FAILED -> {
                        finished = true
                        val reason = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON),
                        )
                        throw Exception("Download failed (code $reason)")
                    }
                    DownloadManager.STATUS_PAUSED -> onProgress("Paused...")
                }
                cursor.close()
            }
            if (!finished) delay(500)
        }

        withContext(Dispatchers.Main) {
            installApk(context, update.version)
        }
    }

    private fun installApk(context: Context, version: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            apkFileName(version),
        )
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun apkFileName(version: String) = "VoltFlowNav-v$version.apk"

    private fun fetchGitHubRelease(): String {
        val connection = (URL(GITHUB_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "VoltFlowNav-UpdateCheck")
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream.bufferedReader().use(BufferedReader::readText)
            if (code !in 200..299) {
                throw Exception("GitHub API: HTTP $code")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun getAppVersion(context: Context): String =
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        } catch (_: Exception) {
            "0.0.0"
        }

}

/** Pure helpers used by [UpdateChecker] and unit tests. */
internal object ReleaseJsonParser {

    fun parseReleaseJson(body: String, currentVersion: String): UpdateInfo? {
        val json = JSONObject(body)
        val tagName = json.optString("tag_name", "").removePrefix("v")
        if (tagName.isEmpty()) {
            throw Exception("No tag_name in GitHub response")
        }
        if (tagName == currentVersion || !isVersionNewer(tagName, currentVersion)) {
            return null
        }

        val assets = json.optJSONArray("assets")
            ?: throw Exception("No assets in release $tagName")

        var apkUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "")
            if (name.endsWith(".apk")) {
                apkUrl = asset.optString("browser_download_url")
                break
            }
        }

        if (apkUrl.isNullOrEmpty()) {
            throw Exception("No APK in release $tagName")
        }

        return UpdateInfo(
            version = tagName,
            downloadUrl = apkUrl,
            releaseNotes = json.optString("body", ""),
        )
    }

    fun isVersionNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").mapNotNull { it.toIntOrNull() }
        val l = local.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(r.size, l.size)) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv > lv) return true
            if (rv < lv) return false
        }
        return false
    }
}
