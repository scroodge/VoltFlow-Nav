package com.bridge.yandexbyd

import android.content.Context

/** Which BYD cluster output path to use. */
enum class DiLinkTarget { AUTO, DILINK3, DILINK5 }

/**
 * DiLink generation detection + manual override.
 *
 * The Yandex input pipeline (accessibility + arrow capture) is identical on every
 * BYD. Only the cluster OUTPUT differs: DiLink 3 has a stock AMap broadcast relay
 * (com.example.amapservice); DiLink 5/6 use the OpenBYD-style broadcast format.
 * AUTO resolves from system properties; the user can force a target.
 */
object DiLink {

    private const val PREFS = "voltflow_dilink"
    private const val KEY_TARGET = "target"

    private fun systemProp(key: String): String = try {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
            .invoke(null, key) as String
    } catch (_: Exception) {
        ""
    }

    /** Hardware detection. DiLink 3.0 is explicit; anything else is treated as DiLink 5/6. */
    fun detect(): DiLinkTarget {
        val s = (systemProp("ro.build.product") + " " +
                systemProp("ro.vehicle.type") + " " +
                systemProp("ro.product.device")).lowercase()
        return if (s.contains("dilink3") || s.contains("di3")) DiLinkTarget.DILINK3
        else DiLinkTarget.DILINK5
    }

    /** Stored override (AUTO = follow detection). */
    fun savedTarget(context: Context): DiLinkTarget {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TARGET, null)
        return runCatching { DiLinkTarget.valueOf(name!!) }.getOrDefault(DiLinkTarget.AUTO)
    }

    fun setTarget(context: Context, target: DiLinkTarget) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TARGET, target.name).apply()
    }

    /** Effective target after resolving AUTO via [detect]. */
    fun resolved(context: Context): DiLinkTarget {
        val saved = savedTarget(context)
        return if (saved == DiLinkTarget.AUTO) detect() else saved
    }
}
