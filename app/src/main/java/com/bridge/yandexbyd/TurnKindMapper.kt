package com.bridge.yandexbyd

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Maps Yandex Navigator instructions to OpenBYD TURN_KIND_* constants
 * (reverse-engineered from com.sr.openbyd v2.2 DEX).
 *
 * Two strategies:
 *   1. fromText()   — keyword matching (RU / EN / TR)
 *   2. fromBitmap() — dark-pixel analysis of the turn-arrow icon
 */
object TurnKindMapper {

    private val RULES: List<Pair<List<String>, String>> = listOf(
        listOf("разворот", "u-turn", "u turn", "geri dön")          to "TURN_KIND_LEFT_BACK",
        listOf("резко налево", "sharp left")                         to "TURN_KIND_LEFT",
        listOf("резко направо", "sharp right")                       to "TURN_KIND_RIGHT",
        listOf("налево", "turn left", "повернуть налево", "sola")    to "TURN_KIND_LEFT",
        listOf("направо", "turn right", "повернуть направо", "sağa") to "TURN_KIND_RIGHT",
        listOf("кольц", "roundabout", "круговое", "döngü")           to "TURN_KIND_RING",
        listOf("паром", "ferry")                                     to "TURN_KIND_INFERRY",
        listOf("прибыли", "arrived", "destination", "vardınız")      to "TURN_KIND_DEST",
        listOf("держитесь левее", "keep left")                       to "TURN_KIND_LEFT_SIDE",
        listOf("держитесь правее", "keep right")                     to "TURN_KIND_RIGHT_SIDE",
        listOf("прямо", "straight", "continue", "düz git")          to "TURN_KIND_STRAIGHT",
    )

    fun fromText(text: String): String? {
        val lower = text.lowercase()
        for ((keywords, kind) in RULES)
            if (keywords.any { lower.contains(it) }) return kind
        return null
    }

    /**
     * Compares dark pixel density in left / right / top sectors
     * to determine which way the arrow icon points.
     */
    fun fromBitmap(bmp: Bitmap): String {
        val w = bmp.width;  val h = bmp.height
        val left   = darkPixels(bmp, 0,     h/4,  w/3,    3*h/4)
        val right  = darkPixels(bmp, 2*w/3, h/4,  w,      3*h/4)
        val top    = darkPixels(bmp, w/4,   0,    3*w/4,  h/3)
        val centre = darkPixels(bmp, w/3,   h/3,  2*w/3,  2*h/3)
        return when {
            left  > right  * 1.6 -> "TURN_KIND_LEFT"
            right > left   * 1.6 -> "TURN_KIND_RIGHT"
            top   > centre * 1.4 -> "TURN_KIND_STRAIGHT"
            else                  -> "TURN_KIND_STRAIGHT"
        }
    }

    private fun darkPixels(bmp: Bitmap, x1: Int, y1: Int, x2: Int, y2: Int): Int {
        var n = 0
        for (x in x1 until x2.coerceAtMost(bmp.width - 1))
            for (y in y1 until y2.coerceAtMost(bmp.height - 1)) {
                val p = bmp.getPixel(x, y)
                val luma = Color.red(p)*299 + Color.green(p)*587 + Color.blue(p)*114
                if (luma < 128_000 && Color.alpha(p) > 64) n++
            }
        return n
    }
}
