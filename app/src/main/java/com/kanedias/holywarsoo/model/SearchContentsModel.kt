package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.SearchTopicResults

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class SearchContentsModel : ViewModel() {
    val page = MutableLiveData<SearchTopicResults>()
    var currentPage: Int = 1
}