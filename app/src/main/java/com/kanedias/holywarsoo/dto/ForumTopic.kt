package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumTopic(
    val id: Int,

    // info
    val anchor: NamedLink,
    val lastMessageUrl: HttpUrl,
    val lastMessageDate: String,
    val sticky: Boolean = false,

    // counters
    val pageCount: Int = -1,
    val replyCount: Int = -1,
    val viewCount: Int = -1,
    val currentPage: Int = -1,

    // child entities
    val messages: List<ForumMessage> = emptyList()
) : Serializable