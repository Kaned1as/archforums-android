package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
data class SearchTopicResults(
    // info
    val name: String,
    val link: HttpUrl,

    // counters
    val pageCount: Int = -1,
    val currentPage: Int = -1,

    // child entities
    val topics: List<ForumTopic> = emptyList()
) : Serializable