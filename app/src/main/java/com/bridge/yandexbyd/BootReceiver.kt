package com.bridge.yandexbyd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * On boot, opens MainActivity so it can self-enable accessibility and re-establish
 * screen capture (the MediaProjection token doesn't survive a reboot). Best-effort:
 * Android may block the background activity start, in which case opening the app
 * once after boot does the same thing.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: Exception) {
                Log.w("YandexBYDBridge", "boot launch blocked: ${e.message}")
            }
        }
    }
}
