package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.card.MaterialCardView
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.misc.showFullscreenFragment

/**
 * View holder representing forum
 *
 * @see MainPageContentFragment
 * @see ForumContentFragment
 *
 * @author Kanedias
 *
 * Created on 23.12.19
 */
class ForumViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.forum_list_item)
    lateinit var forumCard: MaterialCardView

    @BindView(R.id.forum_list_item_separator)
    lateinit var forumCategoryArea: ConstraintLayout

    @BindView(R.id.list_item_separator_text)
    lateinit var forumCategory: TextView

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
        forumCategory.text = forum.category
        lastMessageDate.text = forum.lastMessageDate
        lastMessageTopic.text = forum.lastMessageName

        forumCard.setOnClickListener {
            val fragment = ForumContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ForumContentFragment.FORUM_ARG, forum)
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

            val fragment = TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TopicContentFragment.TOPIC_ARG, topic)
                    putString(TopicContentFragment.URL_ARG, forum.lastMessageLink)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }
    }

}