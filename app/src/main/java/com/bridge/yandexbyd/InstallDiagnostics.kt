package com.bridge.yandexbyd

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InstallDiagnostics {
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun dump(context: Context, trigger: String) {
        if (!BuildConfig.DEBUG) return

        InstallEventLogger.append(context, "=== install_snapshot trigger=$trigger ===")

        runCatching {
            val pm = context.packageManager
            val pkg = context.packageName
            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                ))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
            }

            val now = System.currentTimeMillis()
            val firstInstall = info.firstInstallTime
            val lastUpdate = info.lastUpdateTime
            val secondsSinceInstall = (now - firstInstall) / 1000
            val freshInstall = kotlin.math.abs(firstInstall - lastUpdate) < 2000

            InstallEventLogger.append(context, "build_debug=${BuildConfig.DEBUG}")
            InstallEventLogger.append(context, "build_type=${BuildConfig.BUILD_TYPE}")
            val versionCode = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            InstallEventLogger.append(context, "version=${info.versionName}($versionCode)")
            InstallEventLogger.append(context, "firstInstallTime=${formatTime(firstInstall)}")
            InstallEventLogger.append(context, "lastUpdateTime=${formatTime(lastUpdate)}")
            InstallEventLogger.append(context, "secondsSinceInstall=$secondsSinceInstall")
            InstallEventLogger.append(context, "freshInstall=$freshInstall")
            InstallEventLogger.append(context, "sourceDir=${info.applicationInfo?.sourceDir ?: "?"}")

            val installer = runCatching {
                if (Build.VERSION.SDK_INT >= 30) {
                    pm.getInstallSourceInfo(pkg).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkg)
                }
            }.getOrNull()
            InstallEventLogger.append(context, "installerPackage=$installer")

            val signing = if (Build.VERSION.SDK_INT >= 28) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }
            signing?.forEachIndexed { index, cert ->
                val bytes = cert.toByteArray()
                val sha256 = digestHex(bytes, "SHA-256")
                InstallEventLogger.append(context, "signer[$index]_sha256=$sha256")
            } ?: InstallEventLogger.append(context, "signer=none")

            val enabled = pm.getApplicationEnabledSetting(pkg)
            InstallEventLogger.append(context, "enabledSetting=$enabled")
        }.onFailure { e ->
            InstallEventLogger.append(context, "snapshot_error=${e.javaClass.simpleName}: ${e.message}")
        }

        InstallEventLogger.append(context, "=== end_snapshot ===")
    }

    private fun formatTime(ms: Long): String = timeFormat.format(Date(ms))

    private fun digestHex(bytes: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm).digest(bytes)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
