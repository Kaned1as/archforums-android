package com.kanedias.holywarsoo

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.textview.MaterialTextView
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.misc.layoutVisibilityBool
import com.kanedias.holywarsoo.misc.resolveAttr
import com.kanedias.holywarsoo.misc.showFullscreenFragment


/**
 * View holder that shows forum topic
 *
 * @see TopicContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-22
 */
class TopicViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.topic_name)
    lateinit var topicName: MaterialTextView

    @BindView(R.id.topic_replies_label)
    lateinit var topicRepliesLabel: MaterialTextView

    @BindView(R.id.topic_replies_count)
    lateinit var topicReplies: MaterialTextView

    @BindView(R.id.topic_views_label)
    lateinit var topicViewsLabel: MaterialTextView

    @BindView(R.id.topic_view_count)
    lateinit var topicViews: MaterialTextView

    @BindView(R.id.topic_last_message_topic)
    lateinit var lastMessage: MaterialTextView

    @BindView(R.id.topic_sticky_marker)
    lateinit var stickyMarker: ImageView

    @BindView(R.id.topic_closed_marker)
    lateinit var closedMarker: ImageView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(topic: ForumTopicDesc) {
        stickyMarker.layoutVisibilityBool = topic.sticky
        closedMarker.layoutVisibilityBool = topic.closed
        topicName.text = topic.name

        if (topic.replyCount != null) {
            topicReplies.visibility = View.VISIBLE
            topicRepliesLabel.visibility = View.VISIBLE
            topicReplies.text = topic.replyCount.toString()
        } else {
            topicRepliesLabel.visibility = View.GONE
            topicReplies.visibility = View.GONE
        }

        if (topic.viewCount != null) {
            topicViewsLabel.visibility = View.VISIBLE
            topicViews.visibility = View.VISIBLE
            topicViews.text = topic.viewCount.toString()
        } else {
            topicViewsLabel.visibility = View.GONE
            topicViews.visibility = View.GONE
        }

        if (topic.lastMessageDate.isNullOrEmpty()) {
            lastMessage.visibility = View.GONE
        } else {
            lastMessage.visibility = View.VISIBLE
            lastMessage.text = topic.lastMessageDate
        }

        if (topic.newMessageUrl != null) {
            val color = itemView.resolveAttr(R.attr.colorPrimary)
            lastMessage.setTextColor(color)
            lastMessage.supportCompoundDrawablesTintList = ColorStateList.valueOf(color)
        } else {
            val color = itemView.resolveAttr(R.attr.colorNonImportantText)
            lastMessage.setTextColor(color)
            lastMessage.supportCompoundDrawablesTintList = ColorStateList.valueOf(color)
        }

        itemView.setOnClickListener {
            val relevantUrl = topic.newMessageUrl ?: topic.url

            val fragment = TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TopicContentFragment.URL_ARG, relevantUrl)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }

        lastMessage.setOnClickListener {
            val fragment = TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putString(TopicContentFragment.URL_ARG, topic.lastMessageUrl)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }
    }

}