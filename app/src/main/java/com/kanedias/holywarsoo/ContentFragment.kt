package com.kanedias.holywarsoo

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
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

    abstract fun refreshViews()

    abstract fun refreshContent()
}