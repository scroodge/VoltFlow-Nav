package com.bridge.yandexbyd

import android.content.Context

/**
 * Routes cluster updates to the right sender for the resolved DiLink target.
 * DiLink 3 keeps its exact existing path (AmapBroadcastSender); DiLink 5/6 use
 * Dilink5BroadcastSender. The maneuver/distance/road inputs are identical.
 */
object ClusterBridge {

    fun sendNaviUpdate(
        context: Context,
        iconId: Int,
        segRemainDist: Int,
        roadName: String,
        routeRemainDist: Int = -1,
        routeRemainTime: Int = -1,
        etaText: String? = null,
    ) {
        // User can disable the simulated cluster broadcast if it conflicts with
        // the car's own navigation widget.
        if (!HudSettings.isClusterBroadcastEnabled(context)) return
        when (DiLink.resolved(context)) {
            DiLinkTarget.DILINK5 -> Dilink5BroadcastSender.sendNaviUpdate(
                context, iconId, segRemainDist, roadName, routeRemainDist, routeRemainTime, etaText
            )
            else -> AmapBroadcastSender.sendNaviUpdate(
                context, iconId, segRemainDist, roadName, routeRemainDist, routeRemainTime, etaText
            )
        }
    }

    fun sendNaviStop(context: Context) {
        if (!HudSettings.isClusterBroadcastEnabled(context)) return
        when (DiLink.resolved(context)) {
            DiLinkTarget.DILINK5 -> Dilink5BroadcastSender.sendNaviStop(context)
            else -> AmapBroadcastSender.sendNaviStop(context)
        }
    }
}
