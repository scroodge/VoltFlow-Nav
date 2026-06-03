package com.bridge.yandexbyd

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Fires the AUTONAVI_STANDARD_BROADCAST_SEND intents that OpenBYD (com.sr.openbyd)
 * intercepts and forwards to the BYD instrument cluster.
 *
 * Extra keys and status values reverse-engineered from OpenBYD v2.2 DEX.
 */
object AmapBroadcastSender {

    private const val TAG = "YandexBYDBridge"

    private const val ACTION_UPDATE = "AUTONAVI_STANDARD_BROADCAST_SEND"
    private const val ACTION_STOP   = "AUTONAVI_STANDARD_BROADCAST_STOP"

    // Extra keys (discovered in OpenBYD DEX analysis)
    private const val KEY_STATUS_INFO = "STATUS_INFORMATION"
    private const val KEY_STATUS      = "status"
    private const val KEY_ICON        = "icon"
    private const val KEY_DISTANCE    = "distance"
    private const val KEY_REMAIN_DIS  = "NEXT_SEG_REMAIN_DIS"
    private const val KEY_ROAD_NAME   = "NEXT_ROAD_NAME"
    private const val KEY_NEXT_ICON   = "NEXT_NEXT_TURN_ICON"
    private const val KEY_NEXT_ROAD   = "NEXT_NEXT_ROAD_NAME"

    // Status values (discovered in OpenBYD DEX analysis)
    private const val NAVI_ACTIVE  = "NAVI_STATUS_ACTIVE"
    private const val NAVI_STOPPED = "NAVI_STATUS_STOPPED"
    private const val NAVI_CLOSE   = "NAVI_CLOSE"

    fun sendNaviUpdate(
        context: Context,
        turnKind: String,
        distanceM: Int,
        roadName: String,
        nextTurnKind: String = "TURN_KIND_STRAIGHT",
        nextRoadName: String = ""
    ) {
        val intent = Intent(ACTION_UPDATE).apply {
            putExtra(KEY_STATUS_INFO, NAVI_ACTIVE)
            putExtra(KEY_STATUS,      NAVI_ACTIVE)
            putExtra(KEY_ICON,        turnKind)
            putExtra(KEY_DISTANCE,    distanceM)
            putExtra(KEY_REMAIN_DIS,  distanceM)
            putExtra(KEY_ROAD_NAME,   roadName)
            putExtra(KEY_NEXT_ICON,   nextTurnKind)
            putExtra(KEY_NEXT_ROAD,   nextRoadName)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "Broadcast: $turnKind | $distanceM m | $roadName")
    }

    fun sendNaviStop(context: Context) {
        Intent(ACTION_UPDATE).also {
            it.putExtra(KEY_STATUS_INFO, NAVI_STOPPED)
            it.putExtra(KEY_STATUS,      NAVI_CLOSE)
            context.sendBroadcast(it)
        }
        context.sendBroadcast(Intent(ACTION_STOP))
        Log.d(TAG, "Stop broadcast sent")
    }
}
