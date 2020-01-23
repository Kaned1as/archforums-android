package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.holywarsoo.dto.SearchTopicResults
import com.kanedias.holywarsoo.SearchTopicContentFragment

/**
 * View model for topic search results
 *
 * @see SearchTopicContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-21
 */
class SearchContentsModel : PageableModel() {
    val results = MutableLiveData<SearchTopicResults>()
}