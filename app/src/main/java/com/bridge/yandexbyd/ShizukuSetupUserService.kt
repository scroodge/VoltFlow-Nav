package com.bridge.yandexbyd

import android.os.Build
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Runs in Shizuku's shell (UID 2000) process. Mirrors [setup-car.sh] grants.
 */
class ShizukuSetupUserService : IShizukuSetupService.Stub() {

    override fun grantWriteSecureSettings(packageName: String): Boolean =
        shellOk("pm grant $packageName android.permission.WRITE_SECURE_SETTINGS")

    override fun enableAccessibility(packageName: String, serviceComponent: String): Boolean {
        val current = shellOutput("settings get secure enabled_accessibility_services")
            ?.trim()
            .orEmpty()
        val set = linkedSetOf<String>()
        if (current.isNotEmpty() && current != "null") {
            current.split(':').filter { it.isNotEmpty() }.forEach { set.add(it) }
        }
        set.add(serviceComponent)
        val merged = set.joinToString(":")
        if (!shellOk("settings put secure enabled_accessibility_services $merged")) {
            return false
        }
        return shellOk("settings put secure accessibility_enabled 1")
    }

    override fun allowProjectMedia(packageName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return shellOk("appops set $packageName PROJECT_MEDIA allow")
    }

    private fun shellOutput(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            if (process.waitFor() != 0) null else stdout
        } catch (_: Exception) {
            null
        }
    }

    private fun shellOk(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            process.waitFor() == 0 && !stderr.contains("Exception", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }
}
