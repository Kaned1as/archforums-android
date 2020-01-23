package com.kanedias.holywarsoo.service

import android.text.Spanned
import android.util.LruCache

/**
 * Service for caching spanned strings, as parsing them to markdown can take significant amount of time
 *
 * @author Kanedias
 *
 * Created on 2020-01-11
 */
object SpanCache {

    private val cache = MarkdownLruCache(1024 * 1024)

    fun forMessageId(msgId: Int): Spanned? {
        return cache[msgId]
    }

    fun forMessageId(msgId: Int, slowConverter: () -> Spanned): Spanned {
        val cached = cache[msgId]
        if (cached != null) {
            return cached
        }

        synchronized(cache) {
            val markdown = slowConverter.invoke()
            cache.put(msgId, markdown)

            return markdown
        }
    }

    class MarkdownLruCache(size: Int): LruCache<Int, Spanned>(size) {

        override fun sizeOf(key: Int, value: Spanned): Int {
            return value.length
        }
    }
}