package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.Forum

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
        forumName.text = forum.anchor.name
        forumSubtext.text = forum.subtext
        lastMessageDate.text = forum.lastMessageDate
        lastMessageTopic.text = forum.lastMessage.name

        itemView.setOnClickListener {
            val fragment = ForumContentsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ForumContentsFragment.FORUM_ARG, forum)
                }
            }

            val fm = (itemView.context as AppCompatActivity).supportFragmentManager
            fm.beginTransaction()
                .addToBackStack("showing forum contents")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.main_content_area, fragment)
                .commit()
        }
    }

}