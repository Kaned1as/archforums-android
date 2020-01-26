package com.kanedias.holywarsoo

import androidx.fragment.app.Fragment

/**
 * Abstract fragment containing list content
 *
 * @see ForumContentFragment
 * @see TopicContentFragment
 * @see SearchTopicsContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-29
 */
abstract class ContentFragment: Fragment() {

    abstract fun refreshViews()

    abstract fun refreshContent()
}