package com.kanedias.holywarsoo

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrInterface
import com.r0adkll.slidr.model.SlidrPosition

/**
 * Abstract fragment containing list content
 *
 * @see ForumContentFragment
 * @see TopicContentFragment
 * @see SearchTopicContentFragment
 *
 * @author Kanedias
 *
 * Created on 29.12.19
 */
abstract class ContentFragment: Fragment() {

    /**
     * Slide right to go back helper
     */
    private var slidrInterface: SlidrInterface? = null

    override fun onResume() {
        super.onResume()
        if (hasBackNavigation() && slidrInterface == null) {
            val mainChild = (requireView() as ViewGroup).getChildAt(0)
            slidrInterface = Slidr.replace(mainChild, SlidrConfig.Builder().position(SlidrPosition.LEFT).build())
        }
    }

    open fun hasBackNavigation() = true

    abstract fun refreshViews()

    abstract fun refreshContent()
}