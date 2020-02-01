package com.kanedias.archforums.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.archforums.SearchTopicsContentFragment
import com.kanedias.archforums.dto.ForumTopicDesc
import com.kanedias.archforums.dto.SearchResults

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