package com.kanedias.holywarsoo.model

import androidx.lifecycle.MutableLiveData
import com.kanedias.holywarsoo.dto.Forum

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */
class ForumContentsModel : PageableModel() {
    val forum = MutableLiveData<Forum>()
}