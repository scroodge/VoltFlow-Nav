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

    // The vehicle daemon (and Shizuku) can come up slowly after a cold boot, so
    // poll the binder a few times before giving up instead of failing the first
    // self-start. Matches the 12-attempt local-ADB retry in the OpenBYD proxy.
    private const val BINDER_POLL_ATTEMPTS = 12
    private const val BINDER_POLL_INTERVAL_MS = 1000L

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

    /**
     * Poll [isBinderAlive] up to [BINDER_POLL_ATTEMPTS] times so a slow Shizuku
     * daemon after boot still binds. Runs on the caller thread; keep off the UI
     * thread for the boot path.
     */
    fun awaitBinder(
        attempts: Int = BINDER_POLL_ATTEMPTS,
        intervalMs: Long = BINDER_POLL_INTERVAL_MS,
    ): Boolean {
        repeat(attempts) { i ->
            if (isBinderAlive()) return true
            if (i < attempts - 1) try {
                Thread.sleep(intervalMs)
            } catch (_: InterruptedException) {
                return isBinderAlive()
            }
        }
        return isBinderAlive()
    }

    fun runSetup(
        context: android.content.Context,
        onResult: (Boolean) -> Unit,
    ) {
        if (!isBinderAlive()) {
            // Retry off the UI thread; the daemon may still be starting up.
            Thread {
                val ready = awaitBinder()
                if (ready) runSetup(context, onResult) else onResult(false)
            }.start()
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
                        service.allowProjectMedia(pkg) &&
                        service.allowSystemAlertWindow(pkg)
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

    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

    private val SHIZUKU_PACKAGES = listOf(SHIZUKU_PACKAGE)

    fun launchShizukuApp(context: android.content.Context): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
            ?: return false
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
