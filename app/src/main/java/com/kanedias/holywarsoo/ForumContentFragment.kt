package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.model.ForumContentsModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * @author Kanedias
 *
 * Created on 19.12.19
 */
class ForumContentFragment: ContentFragment() {

    companion object {
        const val FORUM_ARG = "FORUM_ARG"
    }

    @BindView(R.id.topic_list_scroll_area)
    lateinit var forumViewRefresher: SwipeRefreshLayout

    @BindView(R.id.topic_list)
    lateinit var forumView: RecyclerView

    private lateinit var contents: ForumContentsModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_forum_contents, parent, false)
        ButterKnife.bind(this, view)

        forumView.layoutManager = LinearLayoutManager(context)

        forumViewRefresher.setOnRefreshListener { refreshContent() }

        contents = ViewModelProviders.of(this).get(ForumContentsModel::class.java)
        contents.forum.value = requireArguments().getSerializable(FORUM_ARG) as Forum
        contents.forum.observe(this, Observer { forumView.adapter = ForumContentsAdapter(it) })
        contents.forum.observe(this, Observer { refreshViews() })

        refreshContent()

        return view
    }

    override fun refreshViews() {
        val forum = contents.forum.value ?: return
        val activity = activity as? MainActivity ?: return

        activity.addButton.visibility = View.GONE

        activity.toolbar.apply {
            title = forum.name
            subtitle = "${getString(R.string.page)} ${forum.currentPage}"
        }
    }

    override fun refreshContent() {
        lifecycleScope.launchWhenResumed {
            forumViewRefresher.isRefreshing = true

            try {
                val loaded = withContext(Dispatchers.IO) {
                    Network.loadForumContents(contents.forum.value!!, page = contents.currentPage.value!!)
                }

                contents.forum.value = loaded
                contents.currentPage.value = loaded.currentPage
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            forumViewRefresher.isRefreshing = false
        }
    }

    class ForumContentsAdapter(forum: Forum) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val ITEM_SUBFORUM = 0
            const val ITEM_REGULAR = 1
            const val ITEM_UNKNOWN = -1
        }

        private val subforums = forum.subforums
        private val topics = forum.topics


        override fun getItemCount() = topics.size + subforums.size

        override fun getItemViewType(position: Int): Int {
            val subForumCnt = subforums.size
            val topicCnt = topics.size

            return when {
                position < subForumCnt -> ITEM_SUBFORUM
                position < subForumCnt + topicCnt -> ITEM_REGULAR
                else -> ITEM_UNKNOWN
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                ITEM_SUBFORUM -> {
                    val view = inflater.inflate(R.layout.fragment_forum_list_item, parent, false)
                    return ForumViewHolder(view)
                }
                else -> {
                    val view = inflater.inflate(R.layout.fragment_topic_list_item, parent, false)
                    TopicViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                ITEM_SUBFORUM -> {
                    val forum = subforums[position]
                    (holder as ForumViewHolder).apply {
                        setup(forum)
                    }
                }
                else -> {
                    val topic = topics[position - subforums.size]
                    (holder as TopicViewHolder).setup(topic)
                }
            }
        }

    }
}