package com.kanedias.holywarsoo

import android.view.ViewGroup
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kanedias.holywarsoo.model.PageableModel
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

    @BindView(R.id.content_bottom_navigation)
    lateinit var pageNavigation: ViewGroup

    @BindView(R.id.content_list)
    lateinit var contentView: RecyclerView

    @BindView(R.id.content_reply_button)
    lateinit var actionButton: FloatingActionButton

    private lateinit var pageControls: PageViews

    open fun setupUI(model: PageableModel) {
        viewRefresher.setOnRefreshListener { refreshContent() }
        contentView.layoutManager = LinearLayoutManager(context)
        pageControls = PageViews(this, model, pageNavigation)
    }

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