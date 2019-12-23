package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.Forum

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class ForumContentsModel : ViewModel() {
    val forum = MutableLiveData<Forum>()
    var currentPage: Int = 1
}