package com.kanedias.holywarsoo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.misc.setupTheme
import com.kanedias.holywarsoo.service.Config

/**
 * Activity for holding and showing preference fragments
 *
 * @author Kanedias
 *
 * Created on 26.04.18
 */
class SettingsActivity: AppCompatActivity() {

    @BindView(R.id.pref_toolbar)
    lateinit var prefToolbar: Toolbar

    private lateinit var themeChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // theming
        setupTheme()
        themeChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != Config.APP_THEME)
                return@OnSharedPreferenceChangeListener

            lifecycleScope.launchWhenResumed {
                setupTheme()
                recreate()
            }
        }
        Config.prefs.registerOnSharedPreferenceChangeListener(themeChangeListener)

        setContentView(R.layout.activity_preferences)
        ButterKnife.bind(this)

        prefToolbar.title = getString(R.string.menu_settings)
        prefToolbar.navigationIcon = DrawerArrowDrawable(this).apply { progress = 1.0f }
        prefToolbar.setNavigationOnClickListener { finish() }
    }

}