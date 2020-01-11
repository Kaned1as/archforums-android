package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.misc.showFullscreenFragment
import com.kanedias.holywarsoo.misc.visibilityBool

/**
 * View holder that shows forum topic
 *
 * @see TopicContentFragment
 *
 * @author Kanedias
 *
 * Created on 22.12.19
 */
class TopicViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.topic_name)
    lateinit var topicName: TextView

    @BindView(R.id.topic_replies_label)
    lateinit var topicRepliesLabel: TextView

    @BindView(R.id.topic_replies_count)
    lateinit var topicReplies: TextView

    @BindView(R.id.topic_views_label)
    lateinit var topicViewsLabel: TextView

    @BindView(R.id.topic_view_count)
    lateinit var topicViews: TextView

    @BindView(R.id.topic_last_message_topic)
    lateinit var lastMessage: TextView

    @BindView(R.id.topic_sticky)
    lateinit var stickyMarker: ImageView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(topic: ForumTopicDesc) {
        stickyMarker.visibilityBool = topic.sticky
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

        if (topic.lastMessageDate.isEmpty()) {
            lastMessage.visibility = View.GONE
        } else {
            lastMessage.visibility = View.VISIBLE
            lastMessage.text = topic.lastMessageDate
        }

        itemView.setOnClickListener {
            val fragment = TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TopicContentFragment.URL_ARG, topic.link)
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