package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.misc.showFullscreenFragment

class ForumViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.forum_name)
    lateinit var forumName: TextView

    @BindView(R.id.forum_subtext)
    lateinit var forumSubtext: TextView

    @BindView(R.id.forum_last_message_date)
    lateinit var lastMessageDate: TextView

    @BindView(R.id.forum_last_message_topic)
    lateinit var lastMessageTopic: TextView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(forum: Forum) {
        forumName.text = forum.name
        forumSubtext.text = forum.subtext
        lastMessageDate.text = forum.lastMessageDate
        lastMessageTopic.text = forum.lastMessageName

        itemView.setOnClickListener {
            val fragment = ForumContentsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ForumContentsFragment.FORUM_ARG, forum)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }

        lastMessageTopic.setOnClickListener {
            val topic = ForumTopic(
                name = forum.lastMessageName,
                link = forum.lastMessageLink,
                lastMessageDate = forum.lastMessageDate,
                lastMessageUrl = forum.lastMessageLink
            )

            val fragment = TopicContentsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TopicContentsFragment.TOPIC_ARG, topic)
                    putBoolean(TopicContentsFragment.LAST_MESSAGE_ARG, true)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }
    }

}