package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.SearchPage

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class SearchContentsModel : ViewModel() {
    val page = MutableLiveData<SearchPage>()
    var currentPage: Int = 1
}