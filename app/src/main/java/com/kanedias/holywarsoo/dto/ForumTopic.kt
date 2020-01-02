package com.kanedias.holywarsoo.dto

import java.io.Serializable

/**
 * Entity representing forum topic.
 * Topics are where messages are shown, sorted by date and divided in pages.
 * Usually topics can be created by any logged in user on any forum unless it has special access restrictions.
 *
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumTopic(
    val id: Int = -1, // set after page is loaded

    // info
    val name: String,
    val link: String,
    val lastMessageUrl: String,
    val lastMessageDate: String,

    /**
     * True if the topic is pinned to the top of the forum
     */
    val sticky: Boolean = false,

    /**
     * True if this topic is in your profile favorites
     */
    val favorite: Boolean = false,

    /**
     * True if you can write messages to this topic
     */
    val writable: Boolean = true,

    // counters
    val pageCount: Int = -1,
    val replyCount: Int = -1,
    val viewCount: Int = -1,
    val currentPage: Int = -1,

    // child entities

    /**
     * Messages that this topic page contains.
     * Only includes topics from [currentPage].
     */
    val messages: List<ForumMessage> = emptyList()
) : Serializable