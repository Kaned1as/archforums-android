package com.kanedias.archforums

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.google.android.material.card.MaterialCardView
import com.kanedias.archforums.dto.ForumMessage
import com.kanedias.archforums.misc.resolveAttr
import com.kanedias.archforums.misc.shareLink
import com.kanedias.archforums.model.PageableModel
import com.kanedias.archforums.model.TopicContentsModel
import com.kanedias.archforums.service.Network
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.HttpUrl

/**
 * Fragment for showing contents of the topic, i.e. list of messages it contains.
 * Also shows post button if the topic itself is writable.
 * Can be navigated with paging controls.
 *
 * @author Kanedias
 *
 * Created on 2019-12-22
 */
class TopicContentFragment: FullscreenContentFragment() {

    companion object {
        const val URL_ARG = "URL_ARG"
    }

    lateinit var contents: TopicContentsModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_contents, parent, false)
        ButterKnife.bind(this, view)

        contents = ViewModelProviders.of(this).get(TopicContentsModel::class.java)
        contents.topic.observe(this, Observer { contentView.adapter!!.notifyDataSetChanged() })
        contents.topic.observe(this, Observer { refreshViews() })
        contents.refreshed.value = false

        setupUI(contents)
        refreshContent()

        return view
    }

    /**
     * Handle content view scroll after content has been loaded
     */
    private fun handleScroll() {
        val topic = contents.topic.value ?: return

        // highlight custom message if original query mentioned it
        val messageId = HttpUrl.parse(topic.refererLink)!!.queryParameter("pid")
        messageId?.let {
            highlightMessage(it.toInt())
            return
        }

        // scroll after page change
        // if the top message is pinned, scroll to the second
        val firstIdx = topic.messages.firstOrNull()?.index ?: return
        val page = contents.currentPage.value ?: return
        val scrollPos = when {
            page == 1 -> 0 // scroll to the first message on first page in any case
            firstIdx == 1 -> 1 // scroll to the second if first message is pinned
            else -> 0 // to the first, it's not pinned
        }

        contentView.scrollToPosition(scrollPos)
    }

    override fun setupUI(model: PageableModel) {
        super.setupUI(model)

        contentView.adapter = TopicContentsAdapter()
        toolbar.inflateMenu(R.menu.topic_menu)
    }

    private fun manageTopicRelationStatus(action: String): Boolean {
        val topic = contents.topic.value ?: return false

        lifecycleScope.launch {
            Network.perform(
                networkAction = { Network.manageFavorites(topic, action) },
                uiAction = {
                    when(action) {
                        "subscribe" -> {
                            contents.topic.value = topic.copy(isSubscribed = true)
                            Toast.makeText(context, R.string.subscribed, Toast.LENGTH_SHORT).show()
                        }
                        "unsubscribe" -> {
                            contents.topic.value = topic.copy(isSubscribed = false)
                            Toast.makeText(context, R.string.unsubscribed, Toast.LENGTH_SHORT).show()
                        }
                        "favorite" -> {
                            contents.topic.value = topic.copy(isFavorite = true)
                            Toast.makeText(context, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
                        }
                        "unfavorite" -> {
                            contents.topic.value = topic.copy(isFavorite = false)
                            Toast.makeText(context, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            )
        }

        return true
    }

    override fun refreshViews() {
        super.refreshViews()

        val topic = contents.topic.value ?: return

        // setup toolbar content and menu items
        toolbar.apply {
            title = topic.name
            subtitle = getString(R.string.page_n_of_k, topic.currentPage, topic.pageCount)

            if(topic.favoriteLink != null) {
                val favMi = menu.findItem(R.id.menu_topic_favorite)
                favMi.isVisible = true

                when (topic.isFavorite) {
                    true -> favMi // in favorites
                        .setIcon(R.drawable.bookmark_filled).setTitle(R.string.remove_from_favorites)
                        .setOnMenuItemClickListener { manageTopicRelationStatus(action = "unfavorite") }
                    false -> favMi // not in favorites
                        .setIcon(R.drawable.bookmark_add).setTitle(R.string.add_to_favorites)
                        .setOnMenuItemClickListener { manageTopicRelationStatus(action = "favorite") }
                }
            }

            if (topic.subscriptionLink != null) {
                val subMi = menu.findItem(R.id.menu_topic_subscribe)
                subMi.isVisible = true

                when (topic.isSubscribed) {
                    true -> subMi // in subscriptions
                        .setTitle(R.string.unsubscribe)
                        .setOnMenuItemClickListener { manageTopicRelationStatus(action = "unsubscribe") }
                    false -> subMi // not in subscriptions
                        .setTitle(R.string.subscribe)
                        .setOnMenuItemClickListener { manageTopicRelationStatus(action = "subscribe") }
                }
            }

            val shareMi = menu.findItem(R.id.menu_topic_share)
            shareMi.isVisible = true
            shareMi.setOnMenuItemClickListener { context.shareLink(topic.link); true }
        }

        if (topic.isWritable) {
            actionButton.show()
            actionButton.setOnClickListener {
                val frag = AddMessageFragment().apply {
                    arguments = Bundle().apply { putSerializable(AddMessageFragment.TOPIC_ID_ARG, topic.id) }
                }
                frag.show(fragmentManager!!, "reply fragment")
            }
        } else {
            actionButton.visibility = View.GONE
        }

        when (topic.pageCount) {
            1 -> pageNavigation.visibility = View.GONE
            else -> pageNavigation.visibility = View.VISIBLE
        }
    }

    override fun refreshContent() {
        Log.d("TopicFrag", "Refreshing content, topic ${contents.topic.value?.name}, page ${contents.currentPage.value}")

        lifecycleScope.launchWhenResumed {
            viewRefresher.isRefreshing = true

            val topicUrl =  contents.topic.value?.link
            val customUrl = when(contents.refreshed.value) {
                // only load custom url once
                false -> requireArguments().getString(URL_ARG, null)
                else -> null
            }

            Network.perform(
                networkAction = { Network.loadTopicContents(topicUrl, customUrl, page = contents.currentPage.value!!) },
                uiAction = { loaded ->
                    contents.topic.value = loaded
                    contents.pageCount.value = loaded.pageCount
                    contents.currentPage.value = loaded.currentPage
                    contents.refreshed.value = true

                    // scroll to top or specified message after content is loaded
                    handleScroll()
                }
            )

            viewRefresher.isRefreshing = false
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
            contentView.smoothScrollToPosition(position)

            var limit = 20 // 2 sec
            while(contentView.findViewHolderForAdapterPosition(position) == null) {
                // holder view hasn't been laid out yet
                delay(100)

                limit -= 1

                if (!contentView.layoutManager!!.isSmoothScrolling) {
                    // continue scrolling if stopped and view holder is still not visible
                    contentView.smoothScrollToPosition(position)
                }

                if (limit == 0) {
                    // strange, we waited for message for too long to be viewable
                    Log.e("[TopicContent]", "Couldn't find holder for mid $messageId, this shouldn't happen!")
                    return@launchWhenResumed
                }
            }

            // highlight message by tinting background
            val holder = contentView.findViewHolderForAdapterPosition(position) ?: return@launchWhenResumed
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
            val view = inflater.inflate(R.layout.fragment_topic_message_list_item, parent, false)
            return MessageViewHolder(this@TopicContentFragment, view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val topic = contents.topic.value!!
            val message = topic.messages[position]
            holder.setup(message, topic)
        }

    }
}