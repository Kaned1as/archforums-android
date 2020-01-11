package com.kanedias.holywarsoo.dto

import java.io.Serializable

/**
 * Forum as seen on the main page. An item row with some statistics and last message pages.
 * This can also represent subforums in the forum view.
 *
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumDesc(
    // info
    val name: String,
    val link: String,
    val subtext: String,
    val category: String? = null, // only set for forums on the main page, not for subforums
    val lastMessageName: String?,
    val lastMessageLink: String?,
    val lastMessageDate: String?,

    // statistics
    val topicCount: Int,
    val messageCount: Int
)

/**
 * Forum as seen inside itself, where topics are shown, sorted by date and divided in pages.
 * Forums can also contain subforums. Usually only moderators of the website can create new forums.
 *
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class Forum(
    val id: Int,

    // info
    val name: String,
    val link: String,

    /**
     * True if you can write topics to this forum
     */
    val writable: Boolean,

    // counters
    val pageCount: Int,
    val currentPage: Int,

    // child entities
    val subforums: List<ForumDesc>,
    val topics: List<ForumTopicDesc>
) : Serializable

