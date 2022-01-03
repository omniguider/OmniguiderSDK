package com.omni.navisdk.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

object LocaleManager {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_CHINESE = "zh-TW"
    private const val LANGUAGE_KEY = "language_key"
    @JvmStatic
    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage(c))
    }

    fun setNewLocale(c: Context, language: String): Context {
        persistLanguage(c, language)
        return updateResources(c, language)
    }

    fun getLanguage(c: Context?): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        return prefs.getString(LANGUAGE_KEY, LANGUAGE_CHINESE)
    }

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(c: Context, language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        prefs.edit().putString(LANGUAGE_KEY, language).commit()
    }

    private fun updateResources(context: Context, language: String?): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= 24) config.locales[0] else config.locale
    }
}