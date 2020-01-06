package com.kanedias.holywarsoo.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * @author Kanedias
 *
 * Created on 05.01.20
 */
object Config {

    private const val LAST_VERSION = "last-version"

    lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)
    }

    var lastVersion: Int
        get() = prefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = prefs.edit().putInt(LAST_VERSION, lastVersion).apply()
}