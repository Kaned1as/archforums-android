package com.kanedias.holywarsoo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumMessage

class MessageViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.message_author_name)
    lateinit var messageAuthorName: TextView

    @BindView(R.id.message_date)
    lateinit var messageDate: TextView

    @BindView(R.id.message_index)
    lateinit var messageIndex: TextView

    @BindView(R.id.message_body)
    lateinit var messageBody: TextView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(message: ForumMessage) {
        messageAuthorName.text = message.author
        messageDate.text = message.createdDate
        messageIndex.text = "#${message.index}"
        messageBody.text = message.content
    }

}