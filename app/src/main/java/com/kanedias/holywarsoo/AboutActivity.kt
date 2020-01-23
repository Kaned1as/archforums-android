package com.kanedias.holywarsoo

import android.os.Bundle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife

/**
 * Activity for showing "About this app" info
 *
 * @author Kanedias
 *
 * Created on 2020-01-23
 */
class AboutActivity: ThemedActivity() {

    @BindView(R.id.about_toolbar)
    lateinit var aboutToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        aboutToolbar.title = getString(R.string.app_name)
        aboutToolbar.navigationIcon = DrawerArrowDrawable(this).apply { progress = 1.0f }
        aboutToolbar.setNavigationOnClickListener { finish() }
    }

}