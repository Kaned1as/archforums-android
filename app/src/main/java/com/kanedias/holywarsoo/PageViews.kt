package com.kanedias.holywarsoo

import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.misc.visibilityBool
import com.kanedias.holywarsoo.model.PageableModel

/**
 * Fragment to hold all paging-related functions in all paged views where possible.
 *
 * @author Kanedias
 *
 * Created on 07.04.18
 */
class PageViews(parent: ContentFragment, model: PageableModel, iv: View) {

    @BindView(R.id.page_navigation_to_first_page)
    lateinit var toFirstPage: ImageView

    @BindView(R.id.page_navigation_to_previous_page)
    lateinit var toPrevPage: ImageView

    @BindView(R.id.page_navigation_current_page)
    lateinit var currentPage: TextView

    @BindView(R.id.page_navigation_to_next_page)
    lateinit var toNextPage: ImageView

    @BindView(R.id.page_navigation_to_last_page)
    lateinit var toLastPage: ImageView

    init {
        ButterKnife.bind(this, iv)

        // remember to first set pageCount, then currentPage in parent fragment
        model.currentPage.observe(parent, Observer { currentPage.text = it.toString() })
        model.currentPage.observe(parent, Observer { toFirstPage.visibilityBool = it > 1 })
        model.currentPage.observe(parent, Observer { toPrevPage.visibilityBool = it > 1 })
        model.currentPage.observe(parent, Observer { toNextPage.visibilityBool = it < model.pageCount.value!! })
        model.currentPage.observe(parent, Observer { toLastPage.visibilityBool = it < model.pageCount.value!! })

        toFirstPage.setOnClickListener { model.currentPage.value = 1; parent.refreshContent() }
        toPrevPage.setOnClickListener { model.currentPage.value = model.currentPage.value!! - 1; parent.refreshContent() }
        toNextPage.setOnClickListener { model.currentPage.value = model.currentPage.value!! + 1; parent.refreshContent() }
        toLastPage.setOnClickListener { model.currentPage.value = model.pageCount.value!!; parent.refreshContent() }
    }
}