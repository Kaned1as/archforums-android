package com.kanedias.holywarsoo

import androidx.fragment.app.Fragment

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