package com.bridge.yandexbyd

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocale {

    const val BE = "be"
    const val EN = "en"
    const val RU = "ru"

    private const val PREF = "voltflow_locale"
    private const val KEY = "lang"
    private val DEFAULT = BE

    fun getTag(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, DEFAULT)
            ?: DEFAULT

    fun setTag(context: Context, tag: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, tag)
            .apply()
    }

    fun wrap(context: Context): Context {
        val locale = Locale.forLanguageTag(getTag(context))
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
