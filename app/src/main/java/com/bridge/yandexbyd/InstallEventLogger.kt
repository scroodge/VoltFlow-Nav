package com.bridge.yandexbyd

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InstallEventLogger {
    private const val LOG_DIR_NAME = "debug-logs"
    private const val LOG_FILE_NAME = "install-events.log"
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun append(context: Context, event: String) {
        if (!BuildConfig.DEBUG) return

        runCatching {
            val dir = File(context.getExternalFilesDir(null), LOG_DIR_NAME)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val logFile = File(dir, LOG_FILE_NAME)
            val line = "${timeFormat.format(Date())} | sdk=${Build.VERSION.SDK_INT} | $event\n"
            logFile.appendText(line)
        }
    }

    fun debugLogDir(context: Context): File? {
        return context.getExternalFilesDir(LOG_DIR_NAME)
    }
}
