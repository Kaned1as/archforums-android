package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.holywarsoo.SearchTopicsContentFragment
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.dto.SearchResults

/**
 * View model for topic search results
 *
 * @see SearchTopicsContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-21
 */
class SearchTopicsContentsModel : PageableModel() {
    val search = MutableLiveData<SearchResults<ForumTopicDesc>>()
}