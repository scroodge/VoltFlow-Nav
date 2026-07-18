package com.bridge.yandexbyd

import android.content.Context

/**
 * User-facing HUD bridge toggles, persisted in SharedPreferences.
 *
 * [isClusterBroadcastEnabled] gates the simulated AMap cluster broadcast (the
 * AUTONAVI_STANDARD_BROADCAST_SEND that drives the BYD instrument-cluster
 * navigation widget). Default ON. Turning it OFF lets users stop the simulated
 * notifications if they conflict with the car's own navigation widget while a
 * trip is in progress — mirrors OpenBYD's "hud_amap_broadcast_enabled" switch.
 */
object HudSettings {

    private const val PREFS = "voltflow_hud"
    private const val KEY_CLUSTER_BROADCAST = "cluster_broadcast_enabled"

    fun isClusterBroadcastEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_CLUSTER_BROADCAST, true)

    fun setClusterBroadcastEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_CLUSTER_BROADCAST, enabled).apply()
    }
}
