package com.bridge.yandexbyd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * On boot, opens MainActivity so it can self-enable accessibility and re-establish
 * screen capture (the MediaProjection token doesn't survive a reboot). Android 10
 * silently drops this background activity start (no exception, "Abort background
 * activity starts" in logcat) unless the app holds SYSTEM_ALERT_WINDOW — granted
 * via the Shizuku setup flow.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_FROM_BOOT, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            } catch (e: Exception) {
                Log.w("VoltFlowNav", "boot launch blocked: ${e.message}")
            }
        }
    }
}
