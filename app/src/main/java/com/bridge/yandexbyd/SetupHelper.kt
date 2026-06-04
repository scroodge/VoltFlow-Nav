package com.bridge.yandexbyd

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils

object SetupHelper {

    private const val OP_PROJECT_MEDIA = "android:project_media"

    const val ADB_GRANT_CMD =
        "adb shell pm grant com.bridge.yandexbyd android.permission.WRITE_SECURE_SETTINGS"

    private const val ACTION_ACCESSIBILITY_DETAILS_SETTINGS =
        "android.settings.ACCESSIBILITY_DETAILS_SETTINGS"

    fun accessibilityServiceComponent(context: Context): ComponentName =
        ComponentName(context.packageName, "${context.packageName}.YandexA11yService")

    /**
     * Opens the system screen for this app's accessibility service.
     * Falls back to the general accessibility list if the details screen is unavailable.
     */
    fun openAccessibilitySettings(context: Context): Boolean {
        val component = accessibilityServiceComponent(context)
        val details = Intent(ACTION_ACCESSIBILITY_DETAILS_SETTINGS).apply {
            putExtra(Intent.EXTRA_COMPONENT_NAME, component.flattenToString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchIfResolvable(context, details)) return true
        val list = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIfResolvable(context, list)
    }

    private fun launchIfResolvable(context: Context, intent: Intent): Boolean {
        val pm = context.packageManager
        if (intent.resolveActivity(pm) == null) return false
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun hasWriteSecureSettings(context: Context): Boolean {
        val probe = "voltflow_nav_probe_${System.currentTimeMillis()}"
        return try {
            Settings.Secure.putString(context.contentResolver, probe, "1")
            Settings.Secure.putString(context.contentResolver, probe, null)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return !TextUtils.isEmpty(flat) && flat.contains(context.packageName)
    }

    fun tryEnableAccessibility(context: Context): Boolean {
        if (!hasWriteSecureSettings(context)) return false
        return try {
            val component = accessibilityServiceComponent(context).flattenToString()
            val current = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).orEmpty()
            val set = current.split(':').filter { it.isNotEmpty() }.toMutableSet()
            if (set.add(component)) {
                Settings.Secure.putString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    set.joinToString(":")
                )
            }
            Settings.Secure.putInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                1
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isProjectMediaAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            OP_PROJECT_MEDIA,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun tryAllowProjectMedia(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        if (!hasWriteSecureSettings(context)) return false
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val setMode = AppOpsManager::class.java.getMethod(
                "setMode",
                String::class.java,
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            setMode.invoke(
                appOps,
                OP_PROJECT_MEDIA,
                android.os.Process.myUid(),
                context.packageName,
                AppOpsManager.MODE_ALLOWED
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isBatteryUnrestricted(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun batterySettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}
