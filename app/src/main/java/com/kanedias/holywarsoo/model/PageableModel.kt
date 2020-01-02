package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Abstract model that can be navigated by changing pages
 *
 * @author Kanedias
 *
 * Created on 02.01.20
 */
abstract class PageableModel: ViewModel() {
    val currentPage = MutableLiveData(1)
    val pageCount = MutableLiveData(1)
}