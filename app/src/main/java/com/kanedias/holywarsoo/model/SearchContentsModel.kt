package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.SearchResults

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class SearchContentsModel : ViewModel() {
    val page = MutableLiveData<SearchResults>()
    var currentPage: Int = 1
}