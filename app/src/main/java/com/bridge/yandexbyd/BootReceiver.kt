package com.bridge.yandexbyd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("YandexBYDBridge", "Boot completed — bridge service ready")
            // NotificationListenerService is auto-managed by the OS.
            // Add any startup state reset here if needed in future.
        }
    }
}
