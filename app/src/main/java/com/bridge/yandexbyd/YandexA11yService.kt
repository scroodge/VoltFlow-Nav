package com.bridge.yandexbyd

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File
import java.io.FileOutputStream

/**
 * Reads Yandex Navigator's on-screen turn panel (it exposes nothing via
 * notification / MediaSession) and forwards each maneuver to the BYD cluster.
 *
 * Field → resource-id mapping is documented in YANDEX_UI.md. The turn DIRECTION
 * is an unlabeled image, so Phase 1 sends a placeholder STRAIGHT icon; arrow
 * classification is Phase 2.
 *
 * Enable over ADB (the car blocks the Settings screen):
 *   adb shell settings put secure enabled_accessibility_services \
 *       com.bridge.yandexbyd/com.bridge.yandexbyd.YandexA11yService
 *   adb shell settings put secure accessibility_enabled 1
 */
class YandexA11yService : AccessibilityService() {

    companion object {
        const val TAG = "YandexBYDBridge"
        const val YANDEX_PKG = "ru.yandex.yandexnavi"

        private const val ID_DIST = "$YANDEX_PKG:id/text_maneuverballoon_distance"
        private const val ID_METRICS = "$YANDEX_PKG:id/text_maneuverballoon_metrics"
        private const val ID_NEXTSTREET = "$YANDEX_PKG:id/text_nextstreet"
        private const val ID_ETA_DIST = "$YANDEX_PKG:id/textview_eta_distance"
        private const val ID_ETA_TIME = "$YANDEX_PKG:id/textview_eta_time"
        private const val ID_ETA_ARRIVAL = "$YANDEX_PKG:id/textview_eta_arrival"
        private const val ID_MANEUVER_IMG = "$YANDEX_PKG:id/image_maneuverballoon_maneuver"
    }

    private var lastSignature = ""
    private var navActive = false
    private var lastEmitMs = 0L
    private var lastArrowSaveMs = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != YANDEX_PKG) return
        // Throttle: the turn panel fires content-changed events very frequently.
        val now = System.currentTimeMillis()
        if (now - lastEmitMs < 300) return

        val roots = collectRoots()
        try {
            val distNum = firstText(roots, ID_DIST)
            val metrics = firstText(roots, ID_METRICS)

            if (distNum == null) {
                // No maneuver balloon on screen — guidance likely ended.
                if (navActive) {
                    navActive = false
                    lastSignature = ""
                    Log.d(TAG, "maneuver panel gone — STOP")
                    AmapBroadcastSender.sendNaviStop(this)
                }
                return
            }

            val segDist = parseDistance(distNum, metrics)
            val road = firstText(roots, ID_NEXTSTREET).orEmpty()
            val routeDist = firstText(roots, ID_ETA_DIST)?.let { parseDistance(it, null) } ?: -1
            val routeTime = firstText(roots, ID_ETA_TIME)?.let { parseSeconds(it) } ?: -1
            val eta = firstText(roots, ID_ETA_ARRIVAL)

            // Direction is image-only: crop the arrow region and classify it.
            val icon = classifyArrow(roots, segDist)
            val roadLatin = Translit.transliterate(road)

            val signature = "$icon|$segDist|$roadLatin"
            if (signature == lastSignature) return
            lastSignature = signature
            lastEmitMs = now
            navActive = true

            Log.d(TAG, "PANEL dist=$distNum$metrics -> ${segDist}m road='$road'->'$roadLatin' " +
                    "routeDist=$routeDist routeTime=$routeTime eta=$eta")

            AmapBroadcastSender.sendNaviUpdate(
                context = this,
                iconId = icon,
                segRemainDist = segDist,
                roadName = roadLatin,
                routeRemainDist = routeDist,
                routeRemainTime = routeTime,
                etaText = eta,
            )
        } finally {
            roots.forEach { runCatching { it.recycle() } }
        }
    }

    override fun onInterrupt() {}

    /** Crop the maneuver-arrow region from the captured frame and classify it. */
    private fun classifyArrow(roots: List<AccessibilityNodeInfo>, segDist: Int): Int {
        val r = firstBounds(roots, ID_MANEUVER_IMG) ?: return AmapIconMapper.STRAIGHT
        // Pad slightly so the arrowhead isn't clipped.
        val pad = (r.width() * 0.1f).toInt()
        val crop = CaptureService.crop(r.left - pad, r.top - pad, r.right + pad, r.bottom + pad)
            ?: run {
                if (!CaptureService.isReady())
                    Log.d(TAG, "arrow: capture not ready (grant screen capture in the app)")
                return AmapIconMapper.STRAIGHT
            }
        val icon = ManeuverClassifier.classify(crop)
        saveDebugArrow(crop, icon, segDist)
        return icon
    }

    private fun firstBounds(roots: List<AccessibilityNodeInfo>, viewId: String): Rect? {
        for (root in roots) {
            for (n in root.findAccessibilityNodeInfosByViewId(viewId)) {
                val rect = Rect()
                n.getBoundsInScreen(rect)
                if (rect.width() > 0 && rect.height() > 0) return rect
            }
        }
        return null
    }

    /** Save the arrow crop for offline classifier calibration (throttled). */
    private fun saveDebugArrow(bmp: Bitmap, icon: Int, segDist: Int) {
        val now = System.currentTimeMillis()
        if (now - lastArrowSaveMs < 1500) return
        lastArrowSaveMs = now
        try {
            val dir = File(getExternalFilesDir(null), "arrows").apply { mkdirs() }
            val f = File(dir, "arrow_icon${icon}_${segDist}m_${System.currentTimeMillis()}.png")
            FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: Exception) {
            Log.w(TAG, "save debug arrow failed: ${e.message}")
        }
    }

    /** Root node of every window (handles Yandex running in split-screen). */
    private fun collectRoots(): List<AccessibilityNodeInfo> {
        val list = ArrayList<AccessibilityNodeInfo>()
        rootInActiveWindow?.let { list.add(it) }
        for (w in windows) w.root?.let { if (it !in list) list.add(it) }
        return list
    }

    private fun firstText(roots: List<AccessibilityNodeInfo>, viewId: String): String? {
        for (root in roots) {
            val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
            for (n in nodes) {
                val t = n.text?.toString()?.trim()
                if (!t.isNullOrEmpty()) return t
            }
        }
        return null
    }

    /** "100" + " м" → 100 ; "2,7" + " км" → 2700 ; also parses "2,7 км" in one string. */
    private fun parseDistance(value: String, unit: String?): Int {
        val combined = (value + " " + (unit ?: "")).lowercase()
        val num = Regex("""(\d+[.,]?\d*)""").find(combined)?.groupValues?.get(1)
            ?.replace(',', '.')?.toFloatOrNull() ?: return -1
        val isKm = combined.contains("км") || combined.contains("km")
        return if (isKm) (num * 1000).toInt() else num.toInt()
    }

    /** "5 мин", "1 ч 20 мин" → seconds. */
    private fun parseSeconds(s: String): Int {
        var total = 0
        Regex("""(\d+)\s*ч""").find(s)?.let { total += it.groupValues[1].toInt() * 3600 }
        Regex("""(\d+)\s*мин""").find(s)?.let { total += it.groupValues[1].toInt() * 60 }
        return if (total > 0) total else -1
    }
}
