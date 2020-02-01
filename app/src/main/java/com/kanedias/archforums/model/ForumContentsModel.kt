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
    val forum = MutableLiveData<Forum>()
}