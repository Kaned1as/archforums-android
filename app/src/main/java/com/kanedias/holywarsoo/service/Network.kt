package com.kanedias.holywarsoo.service

import com.kanedias.holywarsoo.BuildConfig
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.dto.NamedLink
import com.kanedias.holywarsoo.misc.sanitizeInt
import com.kanedias.holywarsoo.misc.trySanitizeInt
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

/**
 * @author Kanedias
 *
 * Created on 17.12.19
 */
object Network {
    private const val USER_AGENT = "Holywarsoo Android ${BuildConfig.VERSION_NAME}"
    private val MAIN_HOLYWARSOO_URL = HttpUrl.parse("https://holywarsoo.net")!!

    private val userAgent = Interceptor { chain ->
        chain.proceed(chain
            .request()
            .newBuilder()
            .header("User-Agent", USER_AGENT)
            .build())
    }

    private var httpClient: OkHttpClient

    init {
        httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool())
            .dispatcher(Dispatcher())
            .addInterceptor(userAgent)
            .build()
    }

    fun loadForumList(): List<Forum> {
        val req = Request.Builder().url(MAIN_HOLYWARSOO_URL).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IllegalStateException("Can't load main page")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        return parseForums(doc)
    }

    /**
     * Loads forum page completely, with all the supporting browsing info.
     *
     * @param forumBase base forum object to enrich, which may have no subforums or topics available
     *                  at the moment of loading
     *
     * @return fully enriched forum instance
     */
    fun loadForumContents(forumBase: Forum): Forum {
        val req = Request.Builder().url(forumBase.anchor.url).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IllegalStateException("Can't load forum contents")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        val subforums = parseForums(doc)
        val topics = parseTopics(doc)

        val pageLinks = doc.select("div#brdmain > div.linkst p.pagelink")
        val currentPage = pageLinks.select("strong").text()
        val pageCount = pageLinks.first().children()
            .mapNotNull { it.ownText().trySanitizeInt() }
            .max()

        return forumBase.copy(
            pageCount = pageCount!!,
            currentPage = currentPage.sanitizeInt(),
            subforums = subforums,
            topics = topics
        )
    }

    fun loadTopicContents(topic: ForumTopic): ForumTopic {
        val req = Request.Builder().url(topic.anchor.url).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IllegalStateException("Can't load topic contents")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        val messages = parseMessages(doc)

        val pageLinks = doc.select("div#brdmain > div.linkst p.pagelink")
        val currentPage = pageLinks.select("strong").text()
        val pageCount = pageLinks.first().children()
            .mapNotNull { it.ownText().trySanitizeInt() }
            .max()

        return topic.copy(
            pageCount = pageCount!!,
            currentPage = currentPage.sanitizeInt(),
            messages = messages
        )
    }

    private fun parseMessages(doc: Document): List<ForumMessage> {
        val messages = mutableListOf<ForumMessage>()
        for (message in doc.select("div#brdmain div.blockpost")) {
            val msgDateLink = message.select("h2 > span > a")
            val msgAuthor = message.select("div.postleft > dl > dt > strong:last-child").text()
            val msgBody = message.select("div.postright > div.postmsg").outerHtml()
            val msgDate = msgDateLink.text()

            val msgUrl = MAIN_HOLYWARSOO_URL.resolve(msgDateLink.attr("href"))!!

            messages.add(ForumMessage(
                id = msgUrl.queryParameter("pid")!!.toInt(),
                author = msgAuthor,
                createdDate = msgDate,
                content = msgBody
            ))
        }

        return messages
    }

    private fun parseTopics(doc: Document): List<ForumTopic> {
        val topics = mutableListOf<ForumTopic>()
        for (topic in doc.select("div#vf div.inbox table tr[class^=row]")) {
            val isSticky = topic.classNames().contains("isticky")
            val topicLink = topic.select("td.tcl > div.tclcon a").first()
            val topicPageCount = topic.select("td.tcl span.pagestext a:last-child").text()
            val topicReplies = topic.select("td.tc2").text()
            val topicViews = topic.select("td.tc3").text()
            val lastMessageLink = topic.select("td.tcr > a")

            val topicUrl = MAIN_HOLYWARSOO_URL.resolve(topicLink.attr("href"))!!
            val lastMessageUrl = MAIN_HOLYWARSOO_URL.resolve(lastMessageLink.attr("href"))!!

            topics.add(
                ForumTopic(
                    id = topicUrl.queryParameter("id")!!.toInt(),
                    sticky = isSticky,
                    anchor = NamedLink(
                        name = topicLink.text(),
                        url = topicUrl
                    ),
                    replyCount = topicReplies.sanitizeInt(),
                    viewCount = topicViews.sanitizeInt(),
                    pageCount = topicPageCount.toIntOrNull() ?: 1,
                    lastMessageUrl = lastMessageUrl,
                    lastMessageDate = lastMessageLink.text()
                )
            )
        }

        return topics
    }

    private fun parseForums(doc: Document): List<Forum> {
        val forums = mutableListOf<Forum>()

        for (forum in doc.select("div#brdmain div.inbox table tr[id^=forum]")) {
            val forumLink = forum.select("td.tcl div > h3 > a")
            val forumSub = forum.select("td.tcl div.forumdesc")
            val lastMessageLink = forum.select("td.tcr > a")
            val lastMessageDate = forum.select("td.tcr > span")

            val forumUrl = MAIN_HOLYWARSOO_URL.resolve(forumLink.attr("href"))!!
            val lastMessageUrl = MAIN_HOLYWARSOO_URL.resolve(lastMessageLink.attr("href"))!!

            forums.add(
                Forum(
                    id = forumUrl.queryParameter("id")!!.toInt(),
                    anchor = NamedLink(
                        name = forumLink.text(),
                        url = forumUrl
                    ),
                    subtext = forumSub.text(),
                    lastMessage = NamedLink(
                        name = lastMessageLink.text(),
                        url = lastMessageUrl
                    ),
                    lastMessageDate = lastMessageDate.text()
                )
            )
        }
        return forums
    }
}