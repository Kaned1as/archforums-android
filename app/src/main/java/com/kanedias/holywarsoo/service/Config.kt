package com.kanedias.holywarsoo.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.kanedias.holywarsoo.misc.resolveMetadataValue

/**
 * Main shared configuration holder class
 *
 * @author Kanedias
 *
 * Created on 2020-01-05
 */
object Config {

    const val LAST_VERSION = "last-version"
    const val HOME_URL = "home-url"
    const val APP_THEME = "application-theme"

    lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)

        if (homeUrl.isEmpty()) {
            // initial run, store value from app metadata
            homeUrl = ctx.resolveMetadataValue("mainWebsiteUrl")
        }
    }

    /**
     * Reset config values to their default state
     */
    fun reset(ctx: Context) {
        homeUrl = ctx.resolveMetadataValue("mainWebsiteUrl")
    }

    var lastVersion: Int
        get() = prefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = prefs.edit().putInt(LAST_VERSION, lastVersion).apply()

    var homeUrl: String
        get() = prefs.getString(HOME_URL, "")!!
        set(homeUrl) = prefs.edit().putString(HOME_URL, homeUrl).apply()

    var appTheme: String
        get() = prefs.getString(APP_THEME, "fire")!!
        set(theme) = prefs.edit().putString(APP_THEME, theme).apply()
}