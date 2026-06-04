package com.bridge.yandexbyd

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * DiLink 5/6 cluster output. Emits AUTONAVI_STANDARD_BROADCAST_SEND in the format
 * OpenBYD uses on DiLink 5/6 (reverse-engineered from com.sr.openbyd HudController):
 * TYPE=8, EXTRA_STATE=8, IS_BYD_MAP=true, and NEW_ICON in OpenBYD's icon numbering
 * (different from DiLink 3's TURN_STRING index).
 *
 * EXPERIMENTAL: broadcast-only path, validated on a DiLink 5 car by a tester. If the
 * DiLink 5 cluster turns out to require the privileged ICarControl IPC (the reason
 * OpenBYD spawns a shell-uid proxy), this would move to a Shizuku UserService.
 */
object Dilink5BroadcastSender {

    private const val TAG = "VoltFlowNav"
    private const val ACTION = "AUTONAVI_STANDARD_BROADCAST_SEND"

    /**
     * Canonical maneuver (AmapIconMapper / AMap TURN_STRING index, what the classifier
     * outputs) → DiLink 5 NEW_ICON (OpenBYD mapTurnKindToAmapBroadcastIcon codes).
     */
    private fun mapIcon(canonical: Int): Int = when (canonical) {
        AmapIconMapper.LEFT -> 6
        AmapIconMapper.SHARP_LEFT -> 6
        AmapIconMapper.RIGHT -> 4
        AmapIconMapper.SHARP_RIGHT -> 4
        AmapIconMapper.SLIGHT_LEFT -> 7
        AmapIconMapper.SLIGHT_RIGHT -> 3
        AmapIconMapper.UTURN -> 5
        AmapIconMapper.ROUNDABOUT -> 8
        AmapIconMapper.DEST -> 12
        else -> 2 // STRAIGHT / CONTINUE / unknown
    }

    fun sendNaviUpdate(
        context: Context,
        iconId: Int,
        segRemainDist: Int,
        roadName: String,
        routeRemainDist: Int = -1,
        routeRemainTime: Int = -1,
        @Suppress("UNUSED_PARAMETER") etaText: String? = null,
    ) {
        val newIcon = mapIcon(iconId)
        val intent = Intent(ACTION).apply {
            putExtra("KEY_TYPE", 10001)
            putExtra("TYPE", 8)
            putExtra("EXTRA_STATE", 8)
            putExtra("EXTRA_IS_FOREGROUND", 0)
            putExtra("IS_BYD_MAP", true)
            putExtra("IS_BYD_BAIDU_MAP", false)
            putExtra("NEW_ICON", newIcon)
            putExtra("SEG_REMAIN_DIS", segRemainDist)
            putExtra("NEXT_ROAD_NAME", roadName)
            putExtra("ROUTE_REMAIN_DIS", if (routeRemainDist >= 0) routeRemainDist else -1)
            putExtra("ROUTE_REMAIN_TIME", if (routeRemainTime >= 0) routeRemainTime else -1)
            putExtra("SEG_REMAIN_DIS_AUTO", fmtDist(segRemainDist))
            putExtra("ROUTE_REMAIN_DIS_AUTO", fmtDist(routeRemainDist))
            putExtra("ROUTE_REMAIN_TIME_AUTO", fmtTime(routeRemainTime))
            putExtra("ROUTE_REMAIN_TIME_STRING", fmtTime(routeRemainTime))
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "→ DiLink5 cluster: new_icon=$newIcon seg=${segRemainDist}m road='$roadName' " +
                "routeDist=$routeRemainDist routeTime=$routeRemainTime")
    }

    fun sendNaviStop(context: Context) {
        val intent = Intent(ACTION).apply {
            putExtra("KEY_TYPE", 10001)
            putExtra("TYPE", 9)
            putExtra("EXTRA_STATE", 1)
            putExtra("EXTRA_IS_FOREGROUND", 1)
            putExtra("IS_BYD_MAP", true)
            putExtra("IS_BYD_BAIDU_MAP", false)
            putExtra("NEW_ICON", -1)
            putExtra("SEG_REMAIN_DIS", -1)
            putExtra("NEXT_ROAD_NAME", "")
            putExtra("ROUTE_REMAIN_DIS", -1)
            putExtra("ROUTE_REMAIN_TIME", -1)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "→ DiLink5 cluster: STOP")
    }

    private fun fmtDist(metres: Int): String = when {
        metres < 0 -> " "
        metres >= 1000 -> String.format("%.1f km", metres / 1000f)
        else -> "$metres m"
    }

    private fun fmtTime(seconds: Int): String {
        if (seconds < 0) return " "
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return when {
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "$m min"
            else -> "<1 min"
        }
    }
}
