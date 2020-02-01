package com.kanedias.archforums.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.archforums.TopicContentFragment
import com.kanedias.archforums.dto.ForumTopic

/**
 * View model for topic contents.
 *
 * @see TopicContentFragment
 * @author Kanedias
 *
 * Created on 2019-12-21
 */
class TopicContentsModel : PageableModel() {
    val topic = MutableLiveData<ForumTopic>()
}