package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.holywarsoo.TopicContentFragment
import com.kanedias.holywarsoo.dto.ForumTopic

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