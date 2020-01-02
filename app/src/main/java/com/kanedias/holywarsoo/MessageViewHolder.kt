package com.kanedias.holywarsoo

import android.os.Bundle
import android.text.format.DateUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.card.MaterialCardView
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.markdown.handleMarkdown
import com.kanedias.holywarsoo.misc.dpToPixel
import com.kanedias.holywarsoo.misc.showToast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.message_area)
    lateinit var messageArea: MaterialCardView

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

    fun setup(message: ForumMessage, topic: ForumTopic) {
        if (message.index == 1) {
            messageArea.cardElevation = dpToPixel(8f, messageArea.context)
        } else {
            messageArea.cardElevation = dpToPixel(2f, messageArea.context)
        }

        messageAuthorName.text = message.author
        messageIndex.text = "#${message.index}"

        try {
            val creationDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(message.createdDate)!!
            messageDate.text = DateUtils.getRelativeTimeSpanString(creationDate.time)
            messageDate.setOnClickListener { it.showToast(message.createdDate) }
        } catch (ex: ParseException) {
            messageDate.text = message.createdDate
        }

        messageBody.handleMarkdown(message.content)
        messageBody.customSelectionActionModeCallback = SelectionEnhancer(message, topic)

        // make text selectable
        // XXX: this is MAGIC: see https://stackoverflow.com/a/56224791/1696844
        messageBody.setTextIsSelectable(false)
        messageBody.measure(-1, -1)
        messageBody.setTextIsSelectable(true)

    }

    inner class SelectionEnhancer(private val message: ForumMessage, private val topic: ForumTopic): ActionMode.Callback {

        private val textView = messageBody

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val text = textView.text.subSequence(textView.selectionStart, textView.selectionEnd)

            when(item.itemId) {
                R.id.menu_reply -> {
                    // open create new message fragment and insert quote
                    val messageAdd = AddMessageFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable(AddMessageFragment.TOPIC_ARG, topic)
                            putString(AddMessageFragment.AUTHOR_ARG, message.author)
                            putString(AddMessageFragment.MSGID_ARG, message.id.toString())
                            putString(AddMessageFragment.QUOTE_ARG, text.toString())
                        }
                    }

                    val activity = itemView.context as AppCompatActivity
                    messageAdd.show(activity.supportFragmentManager, "showing add message fragment")
                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.content_selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onDestroyActionMode(mode: ActionMode) = Unit
    }

}