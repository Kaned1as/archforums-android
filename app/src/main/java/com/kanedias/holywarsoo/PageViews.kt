package com.kanedias.holywarsoo

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.kanedias.holywarsoo.misc.visibilityBool
import com.kanedias.holywarsoo.model.PageableModel
import kotlinx.coroutines.delay

/**
 * Helper fragment to hold all paging-related functions in all paged views where possible.
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

        // jump to arbitrary page on click
        currentPage.setOnClickListener {
            val jumpToPageView = parent.layoutInflater.inflate(R.layout.view_jump_to_page, null) as TextInputEditText
            jumpToPageView.hint = "1 .. ${model.pageCount.value}"
            jumpToPageView.setText(model.currentPage.value!!.toString())

            MaterialAlertDialogBuilder(parent.context)
                .setTitle(R.string.jump_to_page)
                .setView(jumpToPageView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) {_, _ ->
                    val number = jumpToPageView.text.toString().toIntOrNull()
                    if (number != null && number > 0 && number <= model.pageCount.value!!) {
                        model.currentPage.value = number; parent.refreshContent()
                    }
                }
                .show()

            parent.lifecycleScope.launchWhenResumed {
                delay(100) // wait for dialog to be shown
                jumpToPageView.requestFocus()
                val imm = parent.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(jumpToPageView, 0)
            }
        }
    }
}