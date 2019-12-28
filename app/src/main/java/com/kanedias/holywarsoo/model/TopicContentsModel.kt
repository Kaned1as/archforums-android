package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.ForumTopic

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class TopicContentsModel : ViewModel() {
    val topic = MutableLiveData<ForumTopic>()
    val currentPage = MutableLiveData(1)
}