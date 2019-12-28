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
class MainPageModel: ViewModel() {
    val header = MutableLiveData<List<ForumTopic>>()
    val forums = MutableLiveData<List<Forum>>()
    val account = MutableLiveData<String?>()
}