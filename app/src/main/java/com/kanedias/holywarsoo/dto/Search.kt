package com.kanedias.holywarsoo.dto

import java.io.Serializable

/**
 * Entity representing search results. This is what is shown when
 * * you select "Recents" or "New" or "Where I participate" in forum view.
 * * you select "Show my messages"in forum view or invoke global search for keyword.
 *
 * @author Kanedias
 *
 * Created on 2019-12-17
 */
data class SearchResults<T>(
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
    val pageCount: Int,
    val currentPage: Int,

    // child entities

    /**
     * Elements that this search page contains.
     * Only includes topics from [currentPage].
     */
    @Transient
    val results: List<T> = emptyList()
) : Serializable