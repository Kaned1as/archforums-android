package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.holywarsoo.SearchMessagesContentFragment
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.dto.SearchResults

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