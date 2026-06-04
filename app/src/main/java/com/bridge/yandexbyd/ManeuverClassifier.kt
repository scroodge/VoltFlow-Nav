package com.bridge.yandexbyd

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

/**
 * Classifies the Yandex maneuver-arrow bitmap into an AMap NEW_ICON id.
 *
 * Yandex draws a white arrow on a dark balloon: the arrowhead points in the turn
 * direction (top of the image) while the route "stem" drops down one side in the
 * bottom half. So we compare ink mass left-vs-right in the TOP HALF only — the
 * stem in the bottom half must be ignored or it cancels the arrowhead out.
 *
 * Calibrated against real captures (a left turn measured topL≈238 / topR≈131).
 * Left/right/straight are reliable; slight/sharp/U-turn/roundabout need more
 * samples and currently fall through to the nearest of these three.
 */
object ManeuverClassifier {

    private const val TAG = "VoltFlowNav"

    fun classify(bmp: Bitmap): Int {
        val w = bmp.width
        val h = bmp.height
        if (w < 8 || h < 8) return AmapIconMapper.STRAIGHT

        val bg = avgColor(
            bmp.getPixel(1, 1), bmp.getPixel(w - 2, 1),
            bmp.getPixel(1, h - 2), bmp.getPixel(w - 2, h - 2)
        )

        val yTop = (h * 0.5f).toInt()
        val xLeft = 0.40f * w
        val xRight = 0.60f * w
        var topL = 0; var topR = 0; var topC = 0; var n = 0

        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = bmp.getPixel(x, y)
                if (Color.alpha(p) < 40) continue
                if (colorDist(p, bg) < 90) continue
                n++
                if (y < yTop) {
                    when {
                        x < xLeft -> topL++
                        x > xRight -> topR++
                        else -> topC++
                    }
                }
            }
        }

        if (n < (w * h) / 100) {
            Log.d(TAG, "arrow: n=$n (too little ink) -> STRAIGHT")
            return AmapIconMapper.STRAIGHT
        }

        val diff = topL - topR
        val minDiff = (n * 0.05f).toInt()
        val icon = when {
            topL > topR * 1.35f && diff > minDiff -> AmapIconMapper.LEFT
            topR > topL * 1.35f && -diff > minDiff -> AmapIconMapper.RIGHT
            else -> AmapIconMapper.STRAIGHT
        }
        Log.d(TAG, "arrow: n=$n topL=$topL topR=$topR topC=$topC -> icon=$icon")
        return icon
    }

    private fun avgColor(vararg colors: Int): Int {
        var r = 0; var g = 0; var b = 0
        for (c in colors) { r += Color.red(c); g += Color.green(c); b += Color.blue(c) }
        val n = colors.size
        return Color.rgb(r / n, g / n, b / n)
    }

    private fun colorDist(a: Int, b: Int): Int {
        val dr = Color.red(a) - Color.red(b)
        val dg = Color.green(a) - Color.green(b)
        val db = Color.blue(a) - Color.blue(b)
        return Math.sqrt((dr * dr + dg * dg + db * db).toDouble()).toInt()
    }
}
