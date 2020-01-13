package com.kanedias.holywarsoo.dto

import android.text.Spanned

/**
 * Entity representing forum message.
 * Forum messages form all topics, they can contain rich text, quotes, spoilers, images etc.
 * Usually any logged in user can write message in any topic unless it is closed.
 *
 * @author Kanedias
 *
 * Created on 22.12.19
 */
data class ForumMessage(
    /**
     * Unique message identifier. Used in quotes and reports.
     * It is set after page containing this message is loaded.
     */
    val id: Int = -1,

    // info

    /**
     * Author of this message. Can be anonymous.
     */
    val author: String,

    /**
     * Avatar of the author. Null means author doesn't have one.
     */
    val authorAvatarUrl: String? = null,

    /**
     * Creation date of this message. Represented as string, because of the way
     * forum page shows recent dates. Later dates can be parsed as ISO8601.
     */
    val createdDate: String,

    /**
     * Main content of the forum message. Markdown converted to spanned.
     * As spanned strings are not serializable, the forum message is not serializable as well.
     */
    val content: Spanned,

    // counters

    /**
     * Index of this message within the topic.
     * Unique across the topic only.
     */
    val index: Int = -1
)
