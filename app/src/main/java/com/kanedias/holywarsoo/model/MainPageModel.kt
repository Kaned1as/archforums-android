package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.NamedLink

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class MainPageModel: ViewModel() {
    val header = MutableLiveData<List<NamedLink>>()
    val forums = MutableLiveData<List<Forum>>()
}