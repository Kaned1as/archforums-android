package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.misc.visibilityBool

class TopicViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.topic_name)
    lateinit var topicName: TextView

    @BindView(R.id.topic_reply_count)
    lateinit var topicReplies: TextView

    @BindView(R.id.topic_view_count)
    lateinit var topicViews: TextView

    @BindView(R.id.topic_last_message_topic)
    lateinit var lastMessage: TextView

    @BindView(R.id.topic_sticky)
    lateinit var stickyMarker: ImageView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(topic: ForumTopic) {
        stickyMarker.visibilityBool = topic.sticky
        topicName.text = topic.anchor.name
        topicReplies.text = topic.replyCount.toString()
        topicViews.text = topic.viewCount.toString()
        lastMessage.text = topic.lastMessageDate

        itemView.setOnClickListener {
            val fragment = TopicContentsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TopicContentsFragment.TOPIC_ARG, topic)
                }
            }

            val fm = (itemView.context as AppCompatActivity).supportFragmentManager
            fm.beginTransaction()
                .addToBackStack("showing topic contents")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.main_content_area, fragment)
                .commit()
        }
    }

}