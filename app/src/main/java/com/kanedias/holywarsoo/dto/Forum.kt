package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class Forum(
    val id: Int, // set after page is loaded

    // info
    val name: String,
    val link: HttpUrl,
    val subtext: String,
    val lastMessageName: String,
    val lastMessageLink: HttpUrl,
    val lastMessageDate: String,

    // counters
    val topicCount: Int = -1,
    val commentsCount: Int = -1,
    val pageCount: Int = -1,
    val currentPage: Int = -1,

    // child entities
    val subforums: List<Forum> = emptyList(),
    val topics: List<ForumTopic> = emptyList()
) : Serializable