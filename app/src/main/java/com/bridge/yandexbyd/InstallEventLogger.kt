package com.bridge.yandexbyd

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InstallEventLogger {
    private const val PUBLIC_DIR_NAME = "VoltFlowNav"
    private const val LOG_DIR_NAME = "debug-logs"
    private const val LOG_FILE_NAME = "install-events.log"
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun append(context: Context, event: String) {
        if (!BuildConfig.DEBUG) return

        runCatching {
            val dir = logDir(context)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val logFile = File(dir, LOG_FILE_NAME)
            val line = "${timeFormat.format(Date())} | sdk=${Build.VERSION.SDK_INT} | $event\n"
            logFile.appendText(line)
        }
    }

    /**
     * Prefer the public Downloads folder so logs are reachable from the car's
     * built-in file manager without ADB (Downloads/VoltFlowNav/). Android 10 +
     * requestLegacyExternalStorage allows the direct write; fall back to the
     * app-private external dir if that ever fails.
     */
    private fun logDir(context: Context): File {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            PUBLIC_DIR_NAME,
        )
        if (publicDir.exists() || publicDir.mkdirs()) return publicDir
        return File(context.getExternalFilesDir(null), LOG_DIR_NAME)
    }

    fun debugLogDir(context: Context): File = logDir(context)
}
