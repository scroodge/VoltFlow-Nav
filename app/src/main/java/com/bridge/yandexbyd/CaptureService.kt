package com.bridge.yandexbyd

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager

/**
 * Foreground MediaProjection service. Mirrors the display to an ImageReader and,
 * on demand, converts ONLY the requested region (the maneuver arrow, ~100×100)
 * to a Bitmap. It deliberately does NOT keep converting full-screen frames — that
 * allocated an 8 MB bitmap per frame and was getting the process killed on the
 * car's limited RAM.
 *
 *   adb shell appops set com.bridge.yandexbyd PROJECT_MEDIA allow   # optional pre-grant
 */
class CaptureService : Service() {

    companion object {
        const val TAG = "YandexBYDBridge"
        const val EXTRA_RESULT_CODE = "rc"
        const val EXTRA_DATA = "data"
        private const val CH_ID = "capture"

        @Volatile private var inst: CaptureService? = null
        fun isReady(): Boolean = inst?.reader != null
        fun crop(l: Int, t: Int, r: Int, b: Int): Bitmap? = inst?.cropRegion(l, t, r, b)
    }

    private var projection: MediaProjection? = null
    private var vdisplay: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var w = 0
    private var h = 0
    private var dpi = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CH_ID, "Cluster capture", NotificationManager.IMPORTANCE_MIN)
            )
        }
        val notif: Notification = Notification.Builder(this, CH_ID)
            .setContentTitle("Yandex BYD Bridge")
            .setContentText("Reading navigation arrow")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(7, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rc = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>(EXTRA_DATA)
        if (rc == Activity.RESULT_OK && data != null && projection == null) start(rc, data)
        inst = this
        return START_NOT_STICKY
    }

    private fun start(rc: Int, data: Intent) {
        try {
            val mpm = getSystemService(MediaProjectionManager::class.java)
            projection = mpm.getMediaProjection(rc, data)
            val wm = getSystemService(WindowManager::class.java)
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            w = dm.widthPixels; h = dm.heightPixels; dpi = dm.densityDpi

            // maxImages=2, no listener — we pull the latest frame only when needed.
            reader = ImageReader.newInstance(w, h, android.graphics.PixelFormat.RGBA_8888, 2)
            vdisplay = projection!!.createVirtualDisplay(
                "ybyd-cap", w, h, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader!!.surface, null, null
            )
            Log.d(TAG, "capture started ${w}x$h dpi=$dpi")
        } catch (e: Exception) {
            Log.e(TAG, "capture start failed: ${e.message}", e)
        }
    }

    /** Convert only the requested region of the latest frame to a small Bitmap. */
    @Synchronized
    private fun cropRegion(l: Int, t: Int, r: Int, b: Int): Bitmap? {
        val rd = reader ?: return null
        val img = rd.acquireLatestImage() ?: return null
        try {
            val plane = img.planes[0]
            val buf = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride

            val cl = l.coerceIn(0, w - 1)
            val ct = t.coerceIn(0, h - 1)
            val cr = r.coerceIn(cl + 1, w)
            val cb = b.coerceIn(ct + 1, h)
            val cw = cr - cl
            val ch = cb - ct

            val out = IntArray(cw * ch)
            for (y in 0 until ch) {
                var o = (ct + y) * rowStride + cl * pixelStride
                val rowOff = y * cw
                for (x in 0 until cw) {
                    val red = buf.get(o).toInt() and 0xff
                    val green = buf.get(o + 1).toInt() and 0xff
                    val blue = buf.get(o + 2).toInt() and 0xff
                    val alpha = buf.get(o + 3).toInt() and 0xff
                    out[rowOff + x] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                    o += pixelStride
                }
            }
            return Bitmap.createBitmap(out, cw, ch, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            Log.w(TAG, "crop failed: ${e.message}")
            return null
        } finally {
            img.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { vdisplay?.release() }
        runCatching { reader?.close() }
        runCatching { projection?.stop() }
        if (inst === this) inst = null
    }
}
