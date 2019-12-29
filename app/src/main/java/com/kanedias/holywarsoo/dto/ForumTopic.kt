package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumTopic(
    val id: Int = -1, // set after page is loaded

    // info
    val name: String,
    val link: HttpUrl,
    val lastMessageUrl: HttpUrl,
    val lastMessageDate: String,
    val sticky: Boolean = false,
    val writable: Boolean = true,

    // counters
    val pageCount: Int = -1,
    val replyCount: Int = -1,
    val viewCount: Int = -1,
    val currentPage: Int = -1,

    // child entities
    val messages: List<ForumMessage> = emptyList()
) : Serializable