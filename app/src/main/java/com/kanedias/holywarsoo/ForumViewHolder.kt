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
import com.kanedias.holywarsoo.dto.ForumDesc
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.misc.showFullscreenFragment
import com.kanedias.holywarsoo.misc.visibilityBool

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

    fun setup(forumDesc: ForumDesc) {
        forumName.text = forumDesc.name
        forumSubtext.text = forumDesc.subtext
        forumCategory.text = forumDesc.category
        lastMessageDate.text = forumDesc.lastMessageDate
        lastMessageTopic.text = forumDesc.lastMessageName

        forumCard.setOnClickListener {
            val fragment = ForumContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ForumContentFragment.URL_ARG, forumDesc.link)
                }
            }

            (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
        }

        if (!forumDesc.lastMessageDate.isNullOrEmpty()) {
            lastMessageTopic.visibilityBool = true
            lastMessageTopic.setOnClickListener {
                val fragment = TopicContentFragment().apply {
                    arguments = Bundle().apply {
                        putString(TopicContentFragment.URL_ARG, forumDesc.lastMessageLink)
                    }
                }

                (itemView.context as AppCompatActivity).showFullscreenFragment(fragment)
            }
        } else {
            lastMessageTopic.visibilityBool = false
        }
    }

}