package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl

/**
 * @author Kanedias
 *
 * Created on 22.12.19
 */
data class ForumMessage(
    val id: Int,

    // info
    val author: String,
    val authorAvatarUrl: HttpUrl? = null,
    val createdDate: String,
    val content: String,

    // counters
    val index: Int = -1
)
