package com.kanedias.holywarsoo

import androidx.fragment.app.Fragment

/**
 * @author Kanedias
 *
 * Created on 29.12.19
 */
abstract class ContentFragment: Fragment() {

    abstract fun refreshViews()

    abstract fun refreshContent()
}