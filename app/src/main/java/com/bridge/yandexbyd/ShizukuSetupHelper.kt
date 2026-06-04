package com.bridge.yandexbyd

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku

object ShizukuSetupHelper {

    private const val TAG = "VoltFlowNav"
    private const val USER_SERVICE_VERSION = 1
    private const val USER_SERVICE_TAG = "voltflow_setup_v1"

    fun isShizukuInstalled(context: android.content.Context): Boolean {
        val pm = context.packageManager
        return SHIZUKU_PACKAGES.any { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
        }
    }

    fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasShizukuPermission(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestPermission(requestCode: Int) {
        if (hasShizukuPermission()) return
        Shizuku.requestPermission(requestCode)
    }

    fun addPermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
    }

    fun removePermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(listener)
    }

    fun runSetup(
        context: android.content.Context,
        onResult: (Boolean) -> Unit,
    ) {
        if (!isBinderAlive()) {
            onResult(false)
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = IShizukuSetupService.Stub.asInterface(binder) ?: run {
                    onResult(false)
                    return
                }
                val pkg = context.packageName
                val a11y = SetupHelper.accessibilityServiceComponent(context).flattenToString()
                val ok = runCatching {
                    service.grantWriteSecureSettings(pkg) &&
                        service.enableAccessibility(pkg, a11y) &&
                        service.allowProjectMedia(pkg)
                }.getOrDefault(false)
                Shizuku.unbindUserService(userServiceArgs(context), this, true)
                onResult(ok)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                onResult(false)
            }
        }
        try {
            Shizuku.bindUserService(userServiceArgs(context), connection)
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku bindUserService failed: ${e.message}")
            onResult(false)
        }
    }

    private fun userServiceArgs(context: android.content.Context): Shizuku.UserServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(context.packageName, ShizukuSetupUserService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("shizuku_setup")
            .debuggable(false)
            .version(USER_SERVICE_VERSION)
            .tag(USER_SERVICE_TAG)

    private val SHIZUKU_PACKAGES = listOf("moe.shizuku.privileged.api")
}
