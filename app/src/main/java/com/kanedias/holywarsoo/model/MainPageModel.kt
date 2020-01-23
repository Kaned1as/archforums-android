package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.ForumDesc
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.MainPageContentFragment

/**
 * View model for the main page
 *
 * @see MainPageContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-21
 */
class MainPageModel: ViewModel() {
    val header = MutableLiveData<List<ForumTopicDesc>>()
    val forums = MutableLiveData<List<ForumDesc>>()
    val account = MutableLiveData<String?>()
}