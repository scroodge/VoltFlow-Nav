package com.bridge.yandexbyd

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File
import java.io.FileOutputStream

/**
 * Reads the on-screen turn panel of Yandex Navigator AND Yandex Maps (both expose
 * nothing via notification / MediaSession) and forwards each maneuver to the BYD
 * cluster. Both apps share the same MapKit turn-panel resource ids; only the
 * package prefix differs, so ids are resolved per firing package.
 *
 * IMPORTANT: the maneuver balloon only exists on screen when Yandex's
 * Settings → Navigation → "Show turn hints in the corner of the screen" is ON.
 * With it OFF, the hint is drawn inside the map and these ids are absent, so the
 * bridge reads nothing. See SETUP docs.
 *
 * Turn DIRECTION resolution (text-first, pixel-fallback):
 *  1. The maneuver image's contentDescription is read first (deterministic, free).
 *     Some Yandex builds/locales label it ("Поверните направо"); this is how
 *     OpenBYD classifies. On builds that leave it null we fall back to (2).
 *  2. The arrow region is screen-captured and pixel-classified.
 * The resulting icon is then debounced (2 consistent reads) to stop the classifier
 * flickering between similar arrows, and guidance-end is detected by a short
 * expiration window rather than a single missing frame.
 *
 * Enable over ADB (the car blocks the Settings screen):
 *   adb shell settings put secure enabled_accessibility_services \
 *       com.bridge.yandexbyd/com.bridge.yandexbyd.YandexA11yService
 *   adb shell settings put secure accessibility_enabled 1
 */
class YandexA11yService : AccessibilityService() {

    companion object {
        const val TAG = "VoltFlowNav"
        const val YANDEX_NAVI_PKG = "ru.yandex.yandexnavi"
        const val YANDEX_MAPS_PKG = "ru.yandex.yandexmaps"
        val YANDEX_PACKAGES = setOf(YANDEX_NAVI_PKG, YANDEX_MAPS_PKG)

        // Resource-id suffixes (shared by Navigator and Maps); prefixed per package.
        private const val SUF_DIST = "text_maneuverballoon_distance"
        private const val SUF_METRICS = "text_maneuverballoon_metrics"
        private const val SUF_NEXTSTREET = "text_nextstreet"
        private const val SUF_ETA_DIST = "textview_eta_distance"
        private const val SUF_ETA_TIME = "textview_eta_time"
        private const val SUF_ETA_ARRIVAL = "textview_eta_arrival"
        private const val SUF_MANEUVER_IMG = "image_maneuverballoon_maneuver"

        /** How long the balloon may be absent before we declare guidance ended. */
        private const val STOP_DELAY_MS = 4000L
        /** Consecutive equal raw classifications required to change the emitted icon. */
        private const val ICON_STABLE_COUNT = 2
    }

    private var lastSignature = ""
    private var navActive = false
    private var lastEmitMs = 0L
    private var lastArrowSaveMs = 0L
    /** Package of the Yandex app that fired the current event (navi or maps). */
    private var activePkg = YANDEX_NAVI_PKG

    private val handler = Handler(Looper.getMainLooper())
    private var stopScheduled = false
    private val stopRunnable = Runnable {
        stopScheduled = false
        if (navActive) {
            navActive = false
            lastSignature = ""
            resetIconDebounce()
            Log.d(TAG, "maneuver panel gone (timeout) — STOP")
            ClusterBridge.sendNaviStop(this)
        }
    }

    // Icon debounce: only promote a new raw icon to the emitted icon after it has
    // repeated ICON_STABLE_COUNT times, so a single misclassified frame can't flip
    // the cluster arrow.
    private var stableIcon = -1
    private var pendingIcon = -1
    private var pendingCount = 0

    private fun id(suffix: String) = "$activePkg:id/$suffix"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in YANDEX_PACKAGES) return
        activePkg = pkg
        // Throttle: the turn panel fires content-changed events very frequently.
        val now = System.currentTimeMillis()
        if (now - lastEmitMs < 300) return

        val roots = collectRoots()
        try {
            val distNum = firstText(roots, id(SUF_DIST))
            val metrics = firstText(roots, id(SUF_METRICS))

            if (distNum == null) {
                // No balloon this frame. Don't STOP immediately — the panel briefly
                // disappears between screens. Arm a one-shot timeout instead.
                if (navActive && !stopScheduled) {
                    stopScheduled = true
                    handler.postDelayed(stopRunnable, STOP_DELAY_MS)
                }
                return
            }

            // Balloon is back — cancel any pending guidance-end timeout.
            if (stopScheduled) {
                handler.removeCallbacks(stopRunnable)
                stopScheduled = false
            }

            val segDist = parseDistance(distNum, metrics)
            val road = firstText(roots, id(SUF_NEXTSTREET)).orEmpty()
            val routeDist = firstText(roots, id(SUF_ETA_DIST))?.let { parseDistance(it, null) } ?: -1
            val routeTime = firstText(roots, id(SUF_ETA_TIME))?.let { parseSeconds(it) } ?: -1
            val eta = firstText(roots, id(SUF_ETA_ARRIVAL))

            val icon = debounceIcon(resolveIcon(roots, segDist))
            val roadLatin = Translit.transliterate(road)

            val signature = "$icon|$segDist|$roadLatin"
            if (signature == lastSignature) return
            lastSignature = signature
            lastEmitMs = now
            navActive = true

            Log.d(TAG, "PANEL dist=$distNum$metrics -> ${segDist}m road='$road'->'$roadLatin' " +
                    "icon=$icon routeDist=$routeDist routeTime=$routeTime eta=$eta")

            ClusterBridge.sendNaviUpdate(
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

    override fun onDestroy() {
        handler.removeCallbacks(stopRunnable)
        super.onDestroy()
    }

    /**
     * Text-first / pixel-fallback turn icon. Prefer the maneuver image's
     * contentDescription (deterministic; populated on some Yandex builds), and
     * fall back to pixel classification when it is absent or unrecognised.
     */
    private fun resolveIcon(roots: List<AccessibilityNodeInfo>, segDist: Int): Int {
        val cd = firstContentDesc(roots, id(SUF_MANEUVER_IMG))
        if (!cd.isNullOrBlank()) {
            AmapIconMapper.fromText(cd)?.let {
                Log.d(TAG, "icon from text cd='$cd' -> $it")
                return it
            }
        }
        return classifyArrow(roots, segDist)
    }

    /** Smooth out classifier noise; see [stableIcon]. */
    private fun debounceIcon(raw: Int): Int {
        if (stableIcon == -1) {            // first read of a session — adopt immediately
            stableIcon = raw
            pendingIcon = raw
            pendingCount = 0
            return stableIcon
        }
        if (raw == stableIcon) {
            pendingIcon = raw
            pendingCount = 0
            return stableIcon
        }
        if (raw == pendingIcon) {
            if (++pendingCount >= ICON_STABLE_COUNT) {
                stableIcon = raw
                pendingCount = 0
            }
        } else {
            pendingIcon = raw
            pendingCount = 1
        }
        return stableIcon
    }

    private fun resetIconDebounce() {
        stableIcon = -1
        pendingIcon = -1
        pendingCount = 0
    }

    private fun firstContentDesc(roots: List<AccessibilityNodeInfo>, viewId: String): String? {
        for (root in roots) {
            for (n in root.findAccessibilityNodeInfosByViewId(viewId)) {
                val cd = n.contentDescription?.toString()?.trim()
                if (!cd.isNullOrEmpty()) return cd
            }
        }
        return null
    }

    /** Crop the maneuver-arrow region from the captured frame and classify it. */
    private fun classifyArrow(roots: List<AccessibilityNodeInfo>, segDist: Int): Int {
        val r = firstBounds(roots, id(SUF_MANEUVER_IMG)) ?: return AmapIconMapper.STRAIGHT
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
