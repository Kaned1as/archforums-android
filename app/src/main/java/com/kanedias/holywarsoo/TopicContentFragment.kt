package com.kanedias.holywarsoo

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.card.MaterialCardView
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.misc.resolveAttr
import com.kanedias.holywarsoo.model.TopicContentsModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.lang.Exception

/**
 * Fragment for showing contents of the topic, i.e. list of messages it contains.
 * Also shows post button if the topic itself is writable.
 * Can be navigated with paging controls.
 *
 * @author Kanedias
 *
 * Created on 22.12.19
 */
class TopicContentFragment: ContentFragment() {

    companion object {
        const val URL_ARG = "URL_ARG"
    }

    @BindView(R.id.message_list_scroll_area)
    lateinit var topicViewRefresher: SwipeRefreshLayout

    @BindView(R.id.message_list_bottom_navigation)
    lateinit var pageNavigation: ViewGroup

    @BindView(R.id.message_list)
    lateinit var topicView: RecyclerView

    lateinit var contents: TopicContentsModel

    lateinit var pageControls: PageViews

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_topic_contents, parent, false)
        ButterKnife.bind(this, view)

        topicView.layoutManager = LinearLayoutManager(context)
        topicView.adapter = TopicContentsAdapter()

        topicViewRefresher.setOnRefreshListener { refreshContent() }

        contents = ViewModelProviders.of(this).get(TopicContentsModel::class.java)
        contents.topic.observe(this, Observer { topicView.adapter!!.notifyDataSetChanged() })
        contents.topic.observe(this, Observer { refreshViews() })

        pageControls = PageViews(this, contents, pageNavigation)
        refreshContent()

        return view
    }

    override fun refreshViews() {
        val topic = contents.topic.value ?: return
        val activity = activity as? MainActivity ?: return

        activity.toolbar.apply {
            title = topic.name
            subtitle = "${getString(R.string.page)} ${topic.currentPage}"
        }

        if (topic.isWritable) {
            activity.addButton.show()
            activity.addButton.setOnClickListener {
                val frag = AddMessageFragment().apply {
                    arguments = Bundle().apply { putSerializable(AddMessageFragment.TOPIC_ARG, topic) }
                }
                frag.show(fragmentManager!!, "reply fragment")
            }
        } else {
            activity.addButton.visibility = View.GONE
        }

        when (topic.pageCount) {
            1 -> pageNavigation.visibility = View.GONE
            else -> pageNavigation.visibility = View.VISIBLE
        }
    }

    override fun refreshContent() {
        Log.d("TopicFrag", "Refreshing content, topic ${contents.topic.value?.name}, page ${contents.currentPage.value}")

        lifecycleScope.launchWhenResumed {
            topicViewRefresher.isRefreshing = true

            try {
                val topicUrl =  contents.topic.value?.link
                val customUrl = requireArguments().getString(URL_ARG, "")
                val loaded = withContext(Dispatchers.IO) {
                    Network.loadTopicContents(topicUrl, customUrl, page = contents.currentPage.value!!)
                }
                contents.topic.value = loaded
                contents.pageCount.value = loaded.pageCount
                contents.currentPage.value = loaded.currentPage

                // highlight custom message if custom query mentioned it
                customUrl?.let { HttpUrl.parse(it) }?.queryParameter("pid")?.let { highlightMessage(it.toInt()) }
                requireArguments().remove(URL_ARG) // only load custom url once
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            topicViewRefresher.isRefreshing = false
        }
    }

    /**
     * Highlight the message that has the specified id
     *
     * @param messageId unique message identifier, see [ForumMessage.id]
     */
    fun highlightMessage(messageId: Int) {
        val message = contents.topic.value?.messages?.find { it.id == messageId } ?: return
        val position = contents.topic.value?.messages?.indexOf(message) ?: return

        // the problem with highlighting is that in our recycler views all messages are of different height
        // when recycler view is asked to scroll to some position, it doesn't know their height in advance
        // so we have to scroll continually till all the messages have been laid out and parsed
        lifecycleScope.launchWhenResumed {
            topicView.smoothScrollToPosition(position)

            var limit = 20 // 2 sec
            while(topicView.findViewHolderForAdapterPosition(position) == null) {
                // holder view hasn't been laid out yet
                delay(100)

                limit -= 1

                if (!topicView.layoutManager!!.isSmoothScrolling) {
                    // continue scrolling if stopped and view holder is still not visible
                    topicView.smoothScrollToPosition(position)
                }

                if (limit == 0) {
                    // strange, we waited for message for too long to be viewable
                    Log.e("[TopicContent]", "Couldn't find holder for mid $messageId, this shouldn't happen!")
                    return@launchWhenResumed
                }
            }

            // highlight message with tinted background
            val holder = topicView.findViewHolderForAdapterPosition(position) ?: return@launchWhenResumed
            val card = holder.itemView as MaterialCardView
            ValueAnimator.ofArgb(card.resolveAttr(R.attr.colorPrimary), card.resolveAttr(R.attr.colorSecondary)).apply {
                addUpdateListener {
                    card.setCardBackgroundColor(it.animatedValue as Int)
                }
                duration = 1000
                start()
            }
        }
    }

    inner class TopicContentsAdapter : RecyclerView.Adapter<MessageViewHolder>() {

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            val messages = contents.topic.value!!.messages
            return messages[position].id.toLong()
        }

        override fun getItemCount() = contents.topic.value?.messages?.size ?: 0


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.fragment_topic_message_item, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val topic = contents.topic.value!!
            val message = topic.messages[position]
            holder.setup(message, topic)
        }

    }
}