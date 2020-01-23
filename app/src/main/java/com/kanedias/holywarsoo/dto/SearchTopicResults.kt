package com.kanedias.holywarsoo.dto

import java.io.Serializable

/**
 * Entity representing topic search results. This is what is shown when
 * you select "Recents" or "New" or "Where I participate" in forum view.
 *
 * @author Kanedias
 *
 * Created on 2019-12-17
 */
data class SearchTopicResults(
    // info

    /**
     * Name of this search. Usual variants: "Active", "New", "Recent", "Replies"
     */
    val name: String,

    /**
     * Absolute link where these results can be seen
     */
    val link: String,

    // counters
    val pageCount: Int = -1,
    val currentPage: Int = -1,

    // child entities

    /**
     * Topics that this search page contains.
     * Only includes topics from [currentPage].
     */
    val topics: List<ForumTopicDesc> = emptyList()
) : Serializable