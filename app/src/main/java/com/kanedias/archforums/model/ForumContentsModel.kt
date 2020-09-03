package com.kanedias.archforums.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.archforums.ForumContentFragment
import com.kanedias.archforums.dto.Forum


/**
 * View model for the forum contents.
 *
 * @see ForumContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-21
 */
class ForumContentsModel : PageableModel() {

    /**
     * Forum that is being loaded
     */
    val forum = MutableLiveData<Forum>()

    /**
     * Indicate if content was loaded at minimum once
     */
    val refreshed = MutableLiveData<Boolean>()
}