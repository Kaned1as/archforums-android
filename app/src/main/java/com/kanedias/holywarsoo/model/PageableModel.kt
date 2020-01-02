package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author Kanedias
 *
 * Created on 02.01.20
 */
abstract class PageableModel: ViewModel() {
    val currentPage = MutableLiveData(1)
    val pageCount = MutableLiveData(1)
}