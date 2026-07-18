package com.bridge.yandexbyd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InstallDebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!BuildConfig.DEBUG) return

        val action = intent.action ?: "unknown"
        val data = intent.dataString ?: "no-data"
        val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        InstallEventLogger.append(
            context,
            "receiver_action=$action data=$data replacing=$replacing"
        )
        InstallDiagnostics.dump(context, "receiver_$action")
    }
}
