package com.kanedias.holywarsoo.dto

import com.kanedias.holywarsoo.dto.NamedLink
import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class ForumTopic(
    val id: Int,
    val anchor: NamedLink,
    val lastMessageUrl: HttpUrl,
    val lastMessageDate: String,
    val sticky: Boolean = false,
    val pageCount: Int,
    val replyCount: Int,
    val viewCount: Int
) : Serializable