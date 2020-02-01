package com.kanedias.archforums.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.archforums.SearchMessagesContentFragment
import com.kanedias.archforums.dto.ForumMessage
import com.kanedias.archforums.dto.SearchResults

/**
 * View model for message search results
 *
 * @see SearchMessagesContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-26
 */
class SearchMessagesContentsModel : PageableModel() {
    val search = MutableLiveData<SearchResults<ForumMessage>>()
}