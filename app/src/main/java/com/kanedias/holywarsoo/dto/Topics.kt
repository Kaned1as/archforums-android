package com.kanedias.holywarsoo.dto

import java.io.Serializable

/**
 * Forum topic as seen on the forum page. An item row with some statistics and last message info.
 * This can also be seen on topic search pages, see [SearchTopicResults].
 *
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumTopicDesc(
    // info
    val name: String,
    val url: String,

    /**
     * Last message link.
     * When topic has no replies last message link points to the first message.
     * Can be omitted if topic is moved.
     */
    val lastMessageUrl: String?,

    /**
     * Last message date link.
     * When topic has no replies last message date link points to the first message date.
     * Can be omitted if topic is moved.
     */
    val lastMessageDate: String?,

    /**
     * New message link.
     * Present when you are logged in and have unread messages in this topic.
     */
    val newMessageUrl: String?,

    /**
     * True if the topic is pinned to the top of the forum
     */
    val sticky: Boolean = false,

    /**
     * True if the topic is closed and can't be answered to
     */
    val closed: Boolean = false,

    // statistics
    val replyCount: Int?,
    val viewCount: Int?,
    val pageCount: Int
)

/**
 * Entity representing forum topic.
 * Topics are where messages are shown, sorted by date and divided in pages.
 * Usually topics can be created by any logged in user on any forum unless it has special access restrictions.
 *
 * @author Kanedias
 *
 * Created on 11.01.20
 */
data class ForumTopic(
    val id: Int,

    // info
    val name: String,
    val link: String,
    val refererLink: String, // if we were redirected, use original url for tracking

    /**
     * True if this topic is in your profile favorites
     */
    val isFavorite: Boolean,

    /**
     * Non-empty if logged in and forum board has an ability to favorite topics
     */
    val favoriteLink: String?,

    /**
     * True if this topic is in your profile subscriptions
     */
    val isSubscribed: Boolean,

    /**
     * Non-empty if logged in and forum board has an ability to subscribe to topics
     */
    val subscriptionLink: String?,

    /**
     * True if you can write messages to this topic
     */
    val isWritable: Boolean,

    // counters
    val pageCount: Int,
    val currentPage: Int,

    // child entities

    /**
     * Messages that this topic page contains.
     * Only includes topics from [currentPage].
     */
    @Transient
    val messages: List<ForumMessage> = emptyList()

) : Serializable