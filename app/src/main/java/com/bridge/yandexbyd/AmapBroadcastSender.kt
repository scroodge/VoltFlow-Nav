package com.bridge.yandexbyd

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Sends AUTONAVI_STANDARD_BROADCAST_SEND in the exact format the BYD DiLink 3.0
 * cluster relay (com.example.amapservice) consumes — reverse-engineered from
 * AmapService.apk on a Yuan UP. See CLUSTER_PROTOCOL.md.
 *
 * The relay only acts when BYD's own AMap (com.byd.automap) is NOT navigating,
 * so we always identify as a third party (IS_BYD_MAP=false / absent).
 */
object AmapBroadcastSender {

    private const val TAG = "VoltFlowNav"
    private const val ACTION = "AUTONAVI_STANDARD_BROADCAST_SEND"

    // KEY_TYPE values
    private const val KEY_TYPE_GUIDE = 10001   // per-maneuver guidance info
    private const val KEY_TYPE_STATE = 10019   // navigation state change (start/stop)

    // TYPE (naviState) — must be 0 or 1 for a guidance update to be accepted
    private const val NAVI_STATE_GPS_NORMAL = 0

    // EXTRA_STATE for the stop broadcast (9 = stopped, also accepts 12)
    private const val STATE_STOPPED = 9

    /**
     * Push one maneuver to the cluster.
     *
     * @param iconId        NEW_ICON — AMap turn id (see [AmapIconMapper])
     * @param segRemainDist metres to this maneuver
     * @param roadName      road after the maneuver
     * @param routeRemainDist metres left to destination, or -1 if unknown
     * @param routeRemainTime seconds left to destination, or -1 if unknown
     * @param etaText       arrival clock time text, or null
     */
    fun sendNaviUpdate(
        context: Context,
        iconId: Int,
        segRemainDist: Int,
        roadName: String,
        routeRemainDist: Int = -1,
        routeRemainTime: Int = -1,
        etaText: String? = null,
    ) {
        val intent = Intent(ACTION).apply {
            putExtra("KEY_TYPE", KEY_TYPE_GUIDE)
            putExtra("TYPE", NAVI_STATE_GPS_NORMAL)
            putExtra("NEW_ICON", iconId)
            putExtra("SEG_REMAIN_DIS", segRemainDist)
            putExtra("NEXT_ROAD_NAME", roadName)
            if (routeRemainDist >= 0) putExtra("ROUTE_REMAIN_DIS", routeRemainDist)
            if (routeRemainTime >= 0) putExtra("ROUTE_REMAIN_TIME", routeRemainTime)
            // The cluster's arrival-clock slot is narrow and truncates "12:27" to ":2";
            // "25 min" to destination is already shown, so blank this slot ( a space,
            // because the relay turns null/empty into "-1").
            putExtra("ETA_TEXT", " ")
            // Pre-formatted strings — without these the cluster prints "-1 -1 -1".
            putExtra("SEG_REMAIN_DIS_AUTO", fmtDist(segRemainDist))
            putExtra("ROUTE_REMAIN_DIS_AUTO", fmtDist(routeRemainDist))
            putExtra("ROUTE_REMAIN_TIME_AUTO", fmtTime(routeRemainTime))
            putExtra("IS_BYD_MAP", false)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "→ cluster: icon=$iconId seg=${segRemainDist}m road='$roadName' " +
                "routeDist=$routeRemainDist routeTime=$routeRemainTime")
    }

    /** Distance as a display string. Blank (a space) when unknown, so the cluster
     *  shows nothing instead of "-1". */
    private fun fmtDist(metres: Int): String = when {
        metres < 0 -> " "
        metres >= 1000 -> String.format("%.1f km", metres / 1000f)
        else -> "$metres m"
    }

    /** Duration (seconds) as a display string; blank when unknown. */
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

    /** Clear the cluster when navigation ends. */
    fun sendNaviStop(context: Context) {
        val intent = Intent(ACTION).apply {
            putExtra("KEY_TYPE", KEY_TYPE_STATE)
            putExtra("EXTRA_STATE", STATE_STOPPED)
            putExtra("IS_BYD_MAP", false)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "→ cluster: STOP")
    }
}
