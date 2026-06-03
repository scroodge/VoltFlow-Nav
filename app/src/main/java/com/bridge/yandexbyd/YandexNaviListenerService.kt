package com.bridge.yandexbyd

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Captures Yandex Navigator turn-by-turn notifications and
 * forwards them to OpenBYD via AutoNavi broadcast format.
 */
class YandexNaviListenerService : NotificationListenerService() {

    companion object {
        const val TAG = "YandexBYDBridge"
        const val YANDEX_PKG = "ru.yandex.yandexnavi"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != YANDEX_PKG) return

        val extras = sbn.notification.extras
        val title  = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text   = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        Log.d(TAG, "Yandex notif | title=$title | text=$text")

        val distanceM = parseDistance(title)
        val roadName  = text.trim()

        // Try keyword matching first, fall back to icon bitmap analysis
        val turnKind = TurnKindMapper.fromText("$title $text")
            ?: extractIconBitmap(sbn.notification)?.let { TurnKindMapper.fromBitmap(it) }
            ?: "TURN_KIND_STRAIGHT"

        Log.d(TAG, "Resolved: $turnKind @ $distanceM m on '$roadName'")

        AmapBroadcastSender.sendNaviUpdate(
            context   = this,
            turnKind  = turnKind,
            distanceM = distanceM,
            roadName  = roadName
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != YANDEX_PKG) return
        Log.d(TAG, "Yandex nav ended — sending stop broadcast")
        AmapBroadcastSender.sendNaviStop(this)
    }

    private fun extractIconBitmap(notif: Notification): Bitmap? = try {
        notif.getLargeIcon()?.loadDrawable(this)?.let { d ->
            val w = d.intrinsicWidth.coerceIn(48, 256)
            val h = d.intrinsicHeight.coerceIn(48, 256)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            d.setBounds(0, 0, w, h)
            d.draw(Canvas(bmp))
            bmp
        }
    } catch (e: Exception) {
        Log.w(TAG, "Icon bitmap extraction failed: " + e.message); null
    }

    /** Parses "200 м", "1.2 км", "500 m", "1.5 km" → metres, or -1 */
    private fun parseDistance(text: String): Int {
        Regex("""(\d+[.,]\d+)\s*[кk][мm]""").find(text)?.let {
            return (it.groupValues[1].replace(',','.').toFloat() * 1000).toInt()
        }
        Regex("""(\d+)\s*[кk][мm]""").find(text)?.let {
            return it.groupValues[1].toInt() * 1000
        }
        Regex("""(\d+)\s*[мm]\b""").find(text)?.let {
            return it.groupValues[1].toInt()
        }
        return -1
    }
}
