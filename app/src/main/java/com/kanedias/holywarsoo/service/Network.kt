package com.kanedias.holywarsoo.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.kanedias.holywarsoo.BuildConfig
import com.kanedias.holywarsoo.R
import com.kanedias.holywarsoo.dto.*
import com.kanedias.holywarsoo.misc.sanitizeInt
import com.kanedias.holywarsoo.misc.trySanitizeInt
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * The singleton responsible for performing all network-related operations for the application,
 * such as:
 * * Logging in
 * * Retrieving forums, topics, messages
 * * Creating messages, topics
 * * Editing messages, topics
 * * Reporting messages
 * * Reporting errors of loading back
 *
 * All methods in here must be thread-safe.
 *
 * All methods here *may* throw [IOException]
 *
 * @author Kanedias
 *
 * Created on 17.12.19
 */
object Network {
    private const val ACCOUNT_SHARED_PREFS = "account"
    private const val PREF_USERNAME = "username"
    private const val PREF_PASSWORD = "password"

    private const val USER_AGENT = "Holywarsoo Android ${BuildConfig.VERSION_NAME}"
    private val MAIN_HOLYWARSOO_URL = HttpUrl.parse("https://holywarsoo.net")!!

    val FAVORITE_TOPICS_URL = MAIN_HOLYWARSOO_URL.resolve("search.php?action=show_favorites")!!
    val REPLIES_TOPICS_URL = MAIN_HOLYWARSOO_URL.resolve("search.php?action=show_replies")!!
    val NEW_MESSAGES_TOPICS_URL = MAIN_HOLYWARSOO_URL.resolve("search.php?action=show_new")!!
    val RECENT_TOPICS_URL = MAIN_HOLYWARSOO_URL.resolve("search.php?action=show_recent")!!

    private val userAgent = Interceptor { chain ->
        chain.proceed(chain
            .request()
            .newBuilder()
            .header("User-Agent", USER_AGENT)
            .build())
    }

    private lateinit var httpClient: OkHttpClient
    private lateinit var accountInfo: SharedPreferences
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var cookiePersistor: SharedPrefsCookiePersistor

    fun init(ctx: Context) {
        accountInfo = ctx.getSharedPreferences(ACCOUNT_SHARED_PREFS, Context.MODE_PRIVATE)
        cookiePersistor = SharedPrefsCookiePersistor(accountInfo)
        cookieJar = PersistentCookieJar(SetCookieCache(), cookiePersistor)
        httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool())
            .dispatcher(Dispatcher())
            .addInterceptor(userAgent)
            .cookieJar(cookieJar)
            .build()
    }

    private fun authCookie() = cookiePersistor.loadAll().firstOrNull { it.name().startsWith("pun_cookie") }

    fun resolve(url: String) = MAIN_HOLYWARSOO_URL.resolve(url)

    fun daysToAuthExpiration() = authCookie()
        ?.let { it.expiresAt() - System.currentTimeMillis() / 1000 / 60 / 60 / 24 }
        ?: 0

    fun isLoggedIn() = cookiePersistor.loadAll()
        .filter { it.name().startsWith("pun_cookie") }
        .any { it.expiresAt() > System.currentTimeMillis() }

    fun getUsername() = accountInfo.getString(PREF_USERNAME, null)

    /**
     * Only use if [isLoggedIn] returns true and username/password are populated
     * in preferences
     */
    fun refreshLogin() = login(
        accountInfo.getString(PREF_USERNAME, null)!!,
        accountInfo.getString(PREF_PASSWORD, null)!!
    )

    /**
     * Clears cookies in http client, clears saved username and password
     */
    fun logout() {
        cookieJar.clear()
        accountInfo.edit().clear().apply()
    }

    /**
     * Login using specified username and password.
     * Creates persistent cookies and account preferences.
     */
    @Throws(IOException::class)
    fun login(username: String, password: String) {
        // clear previous login information
        cookieJar.clear()

        // login page contains CSRF tokens and other form parameters that should be
        // present in the request for it to be valid. We need to extract this data prior to logging in
        val loginPageUrl = MAIN_HOLYWARSOO_URL.resolve("login.php")!!
        val loginPageReq = Request.Builder().url(loginPageUrl).get().build()
        val loginPageResp = httpClient.newCall(loginPageReq).execute()
        if (!loginPageResp.isSuccessful)
            throw IllegalStateException("Can't load login page")

        val loginPageHtml = loginPageResp.body()!!.string()
        val loginPageDoc = Jsoup.parse(loginPageHtml)

        val loginFormInputs =  loginPageDoc.select("form#login input[type=hidden]")

        val loginReqUrl = loginPageUrl.newBuilder()
            .addQueryParameter("action", "in")

        val reqBody = FormBody.Builder()
            .add("req_username", username)
            .add("req_password", password)
            .add("save_pass", "1")

        for (input in loginFormInputs) {
            reqBody.add(input.attr("name"), input.attr("value"))
        }

        val loginReq = Request.Builder()
            .url(loginReqUrl.build())
            .post(reqBody.build())
            .build()

        val loginResp = httpClient.newCall(loginReq).execute()
        if (!loginResp.isSuccessful)
            throw IOException("Can't authenticate")

        if (authCookie() == null)
            throw IOException("Authentication failed, invalid login/password")

        accountInfo.edit()
            .putString(PREF_USERNAME, username)
            .putString(PREF_PASSWORD, password)
            .apply()
    }

    /**
     * Load forum list from the main page.
     *
     * @return list of parsed forums. It has the same ordering as it had on the actual page.
     */
    @Throws(IOException::class)
    fun loadForumList(): List<Forum> {
        val req = Request.Builder().url(MAIN_HOLYWARSOO_URL).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IOException("Can't load main page")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        return parseForums(doc)
    }

    /**
     * Loads search page completely, with all the supporting browsing info, enriching the existing
     * object. The returned value is a copy of the original.
     *
     * @param resultsBase base search results object to enrich, which may have no topics available
     *                    at the moment of loading
     *
     * @return fully enriched search page instance. Topics have the same ordering as they had on the actual page.
     */
    @Throws(IOException::class)
    fun loadSearchResults(resultsBase: SearchTopicResults, page: Int = 1): SearchTopicResults {
        val pageUrl = resultsBase.link.newBuilder().addQueryParameter("p", page.toString()).build()

        val req = Request.Builder().url(pageUrl).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IOException("Can't load forum contents")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        val topics = parseTopics(doc)

        val pageLinks = doc.select("div#brdmain > div.linkst p.pagelink")
        val currentPage = pageLinks.select("strong").text()
        val pageCount = pageLinks.first().children()
            .mapNotNull { it.ownText().trySanitizeInt() }
            .max()

        return resultsBase.copy(
            pageCount = pageCount!!,
            currentPage = currentPage.sanitizeInt(),
            topics = topics
        )
    }

    /**
     * Loads forum page completely, with all the supporting browsing info, enriching the existing
     * object. The returned value is a copy of the original.
     *
     * @param forumBase base forum object to enrich, which may have no subforums or topics available
     *                  at the moment of loading
     *
     * @return fully enriched forum instance. Topics have the same ordering as they had on the actual page.
     */
    @Throws(IOException::class)
    fun loadForumContents(forumBase: Forum, page: Int = 1): Forum {
        val pageUrl = forumBase.link.newBuilder().addQueryParameter("p", page.toString()).build()

        val req = Request.Builder().url(pageUrl).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IOException("Can't load forum contents")

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

    /**
     * Loads topic page completely, with all the supporting browsing info, enriching the existing
     * object. The returned value is a copy of the original.
     *
     * @param topic base topic object to enrich, which may have no messages or pages available
     *                  at the moment of loading
     *
     * @return fully enriched topic instance. Messages have the same ordering as they had on the actual page.
     */
    @Throws(IOException::class)
    fun loadTopicContents(topic: ForumTopic, link: HttpUrl? = null, page: Int = 1): ForumTopic {
        val pageUrl = link ?: topic.link.newBuilder().addQueryParameter("p", page.toString()).build()

        val req = Request.Builder().url(pageUrl).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IOException("Can't load topic contents")

        val html = resp.body()!!.string()
        val doc = Jsoup.parse(html)

        // topic link is in the page header, topic id can be derived from it
        // topic name is page title and writable bit is derived from the presence of answer button
        val topicRef = doc.select("head link[rel=canonical]").attr("href")
        val topicId = resolve(topicRef)!!.queryParameter("id")!!
        val topicName = doc.select("head title").text()
        val topicWritable = doc.select("div#brdmain div.postlinksb a[href^=post.php]:containsOwn(Ответить)")

        val pageLinks = doc.select("div#brdmain > div.linkst p.pagelink")
        val currentPage = pageLinks.select("strong").text()
        val pageCount = pageLinks.first().children()
            .mapNotNull { it.ownText().trySanitizeInt() }
            .max()

        val messages = parseMessages(doc)

        return topic.copy(
            id = topicId.sanitizeInt(),
            name = topicName,
            writable = topicWritable.isNotEmpty(),
            pageCount = pageCount!!,
            currentPage = currentPage.sanitizeInt(),
            messages = messages
        )
    }


    @Throws(IOException::class)
    fun postMessage(topic: ForumTopic, message: String) {
        val postUrl = resolve("post.php")!!.newBuilder().addQueryParameter("tid", topic.id.toString()).build()

        val req = Request.Builder().url(postUrl).get().build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful)
            throw IOException("Can't load topic reply page")

        val replyPageHtml = resp.body()!!.string()
        val replyPageDoc = Jsoup.parse(replyPageHtml)

        val replyPageInputs = replyPageDoc.select("form#post input[type=hidden]")

        val reqBody = FormBody.Builder()
            .add("req_message", message)

        for (input in replyPageInputs) {
            reqBody.add(input.attr("name"), input.attr("value"))
        }

        val postMessageReq = Request.Builder()
            .url(postUrl)
            .post(reqBody.build())
            .build()

        val postMessageResp = httpClient.newCall(postMessageReq).execute()
        if (!postMessageResp.isSuccessful)
            throw IOException("Unexpected failure")
    }

    /**
     * Parses message list page of the topic and creates a structured list of all messages on the page.
     *
     * @param doc fully parsed document containing page with message list (topic)
     * @return list of parsed messages. It has the same ordering as it had on the actual page.
     */
    private fun parseMessages(doc: Document): List<ForumMessage> {
        val messages = mutableListOf<ForumMessage>()
        for (message in doc.select("div#brdmain div.blockpost")) {
            // all message info is self-contained in the message box
            val msgDateLink = message.select("h2 > span > a")
            val msgIndex = message.select("h2 > span > span.conr").text()
            val msgAuthor = message.select("div.postleft > dl > dt > strong:last-child").text()
            val msgBody = message.select("div.postright > div.postmsg").first()
            val msgDate = msgDateLink.text()

            val msgUrl = MAIN_HOLYWARSOO_URL.resolve(msgDateLink.attr("href"))!!

            messages.add(ForumMessage(
                id = msgUrl.queryParameter("pid")!!.toInt(),
                index = msgIndex.replace("#", "").toInt(),
                author = msgAuthor,
                createdDate = msgDate,
                content = postProcessMessage(msgBody)
            ))
        }

        return messages
    }

    /**
     * Post-processes the message, replaces all JS-based spoiler tags with standard HTML
     * `<details>` + `<summary>` tags that were meant to be actual spoilers, without requiring JS.
     *
     * @param msgBody element representing message body, usually `div.postmsg` in the document
     * @return HTML string after postprocessing
     */
    private fun postProcessMessage(msgBody: Element): String {
        // need to detect all expandable spoiler tags and replace them with
        // standard `details` + `summary`
        for (spoilerTag in msgBody.select("div[onclick*=▼]")) {
            spoilerTag.parent().apply {
                tagName("details") // replace quotebox `div` with `details` tag
                clearAttributes()          // clear all attributes it might have
            }

            spoilerTag.apply {
                tagName("summary") // replace spoiler text `div` with `summary` tag
                clearAttributes()          // clear all attributes it might have
                html(this.ownText())       // we don't need a span with down/up arrow, just spoiler text
            }
        }

        // other post-processing steps may follow, leaving this vacant...

        return msgBody.outerHtml()
    }

    /**
     * Parses topic list page of the forum and creates a structured list of all topics on the page.
     *
     * @param doc fully parsed document containing page with topic list (forum/favorites/active etc.)
     * @return list of parsed forum topics. It has the same ordering as it had on the actual page.
     */
    private fun parseTopics(doc: Document): List<ForumTopic> {
        val viewsClass = doc.select("div#vf div.inbox table thead tr th:containsOwn(Просмотров)").firstOrNull()
        val repliesClass = doc.select("div#vf div.inbox table thead tr th:containsOwn(Ответов)").firstOrNull()

        val topics = mutableListOf<ForumTopic>()
        for (topic in doc.select("div#vf div.inbox table tr[class^=row]")) {

            // topic list page may have different layouts depending on whether it's search page
            // or forum page, so we should be smart about it, detecting row meanings by their names
            val isSticky = topic.classNames().contains("isticky")
            val topicLink = topic.select("td.tcl > div.tclcon a").first()
            val topicPageCount = topic.select("td.tcl span.pagestext a:last-child").text()

            val topicReplies = repliesClass?.let { topic.select("td.${it.className()}").text() } ?: "-1"
            val topicViews = viewsClass?.let { topic.select("td.${it.className()}").text() } ?: "-1"

            val lastMessageLink = topic.select("td.tcr > a")

            val topicUrl = MAIN_HOLYWARSOO_URL.resolve(topicLink.attr("href"))!!
            val lastMessageUrl = MAIN_HOLYWARSOO_URL.resolve(lastMessageLink.attr("href"))!!

            topics.add(
                ForumTopic(
                    id = topicUrl.queryParameter("id")!!.toInt(),
                    sticky = isSticky,
                    name = topicLink.text(),
                    link = topicUrl,
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

    /**
     * Parses forum list page of the site and creates a structured list of
     * all forums encountered on the page.
     *
     * @param doc fully parsed document containing page with forum list (main)
     * @return list of parsed forums. It has the same ordering as it had on the actual page.
     */
    private fun parseForums(doc: Document): List<Forum> {
        val forums = mutableListOf<Forum>()

        for (forum in doc.select("div#brdmain div.inbox table tr[id^=forum]")) {
            // forums can be found in main page and in forum page as well, as a subforums
            // all the info is fortunately self-contained and same across all kinds of pages
            val forumLink = forum.select("td.tcl div > h3 > a")
            val forumSub = forum.select("td.tcl div.forumdesc")
            val lastMessageLink = forum.select("td.tcr > a")
            val lastMessageDate = forum.select("td.tcr > span")

            val forumUrl = MAIN_HOLYWARSOO_URL.resolve(forumLink.attr("href"))!!
            val lastMessageUrl = MAIN_HOLYWARSOO_URL.resolve(lastMessageLink.attr("href"))!!

            forums.add(
                Forum(
                    id = forumUrl.queryParameter("id")!!.toInt(),
                    name = forumLink.text(),
                    link = forumUrl,
                    subtext = forumSub.text(),
                    lastMessageName = lastMessageLink.text(),
                    lastMessageLink = lastMessageUrl,
                    lastMessageDate = lastMessageDate.text()
                )
            )
        }
        return forums
    }

    /**
     * Handle typical network-related problems.
     * @param ctx context to get error string from
     * @param errMapping mapping of ids like `HttpUrlConnection.HTTP_NOT_FOUND -> R.string.not_found`
     */
    fun reportErrors(ctx: Context, ex: Exception) = when (ex) {

        // generic connection-level error, show as-is
        is IOException -> {
            Log.w("Fair/Network", "Connection error", ex)
            val errorText = ctx.getString(R.string.error_connecting)
            Toast.makeText(ctx, "$errorText: ${ex.message}", Toast.LENGTH_SHORT).show()
        }

        else -> throw ex
    }
}