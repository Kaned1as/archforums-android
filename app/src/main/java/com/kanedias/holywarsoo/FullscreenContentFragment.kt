package com.kanedias.holywarsoo

import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrInterface
import com.r0adkll.slidr.model.SlidrPosition

/**
 * @author Kanedias
 *
 * Created on 14.01.20
 */
abstract class FullscreenContentFragment: ContentFragment() {

    @BindView(R.id.main_fragment_content_area)
    lateinit var mainArea: CoordinatorLayout

    @BindView(R.id.content_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.content_scroll_area)
    lateinit var viewRefresher: SwipeRefreshLayout

    override fun refreshViews() {
        // setup toolbar
        toolbar.navigationIcon = DrawerArrowDrawable(activity).apply { progress = 1.0f }
        toolbar.setNavigationOnClickListener { fragmentManager?.popBackStack() }
    }

    /**
     * Slide right to go back helper
     */
    private var slidrInterface: SlidrInterface? = null

    override fun onResume() {
        super.onResume()
        if (slidrInterface == null) {
            slidrInterface = Slidr.replace(mainArea, SlidrConfig.Builder().position(SlidrPosition.LEFT).build())
        }
    }
}