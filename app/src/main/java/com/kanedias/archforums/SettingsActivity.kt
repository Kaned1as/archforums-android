package com.kanedias.archforums

import android.os.Bundle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife

/**
 * Activity for holding and showing preference fragments
 *
 * @author Kanedias
 *
 * Created on 2018-04-26
 */
class SettingsActivity: ThemedActivity() {

    @BindView(R.id.pref_toolbar)
    lateinit var prefToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preferences)
        ButterKnife.bind(this)

        prefToolbar.title = getString(R.string.menu_settings)
        prefToolbar.navigationIcon = DrawerArrowDrawable(this).apply { progress = 1.0f }
        prefToolbar.setNavigationOnClickListener { finish() }
    }

}