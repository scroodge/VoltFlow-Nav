package com.bridge.yandexbyd

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ManeuverClassifierTest {

    @Test
    fun classify_tinyBitmap_isStraight() {
        val bmp = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        assertEquals(AmapIconMapper.STRAIGHT, ManeuverClassifier.classify(bmp))
    }

    @Test
    fun classify_moreInkOnLeftTop_isLeft() {
        val bmp = arrowBitmap(leftHeavy = true)
        assertEquals(AmapIconMapper.LEFT, ManeuverClassifier.classify(bmp))
    }

    @Test
    fun classify_moreInkOnRightTop_isRight() {
        val bmp = arrowBitmap(leftHeavy = false)
        assertEquals(AmapIconMapper.RIGHT, ManeuverClassifier.classify(bmp))
    }

    @Test
    fun classify_balancedTop_isStraight() {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        fillBackground(bmp)
        val yTop = bmp.height / 2
        for (y in 0 until yTop) {
            for (x in bmp.width / 3 until 2 * bmp.width / 3) {
                bmp.setPixel(x, y, Color.WHITE)
            }
        }
        assertEquals(AmapIconMapper.STRAIGHT, ManeuverClassifier.classify(bmp))
    }

    /** Synthetic Yandex-style balloon: dark corners, white ink in the top half. */
    private fun arrowBitmap(leftHeavy: Boolean): Bitmap {
        val w = 64
        val h = 64
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        fillBackground(bmp)
        val yTop = h / 2
        val xStart = if (leftHeavy) 0 else w / 2
        val xEnd = if (leftHeavy) w / 2 else w
        for (y in 0 until yTop) {
            for (x in xStart until xEnd) {
                bmp.setPixel(x, y, Color.WHITE)
            }
        }
        return bmp
    }

    private fun fillBackground(bmp: Bitmap) {
        val dark = Color.rgb(30, 30, 30)
        bmp.eraseColor(dark)
        ApplicationProvider.getApplicationContext<android.content.Context>()
        // Robolectric needs a context touch so the test APK is loaded; no-op use above.
    }
}
