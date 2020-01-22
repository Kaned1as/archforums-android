package com.kanedias.holywarsoo

import android.os.Bundle
import android.text.format.DateUtils
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.markdown.handleMarkdown
import com.kanedias.holywarsoo.misc.*
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * View holder that shows topic message
 *
 * @author Kanedias
 *
 * Created on 22.12.19
 */
class MessageViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

    @BindView(R.id.message_area)
    lateinit var messageArea: MaterialCardView

    @BindView(R.id.message_author_avatar)
    lateinit var messageAvatar: ImageView

    @BindView(R.id.message_author_name)
    lateinit var messageAuthorName: TextView

    @BindView(R.id.message_date)
    lateinit var messageDate: TextView

    @BindView(R.id.message_index)
    lateinit var messageIndex: TextView

    @BindView(R.id.message_body)
    lateinit var messageBody: TextView

    @BindView(R.id.message_overflow_menu)
    lateinit var messageMenu: ImageView

    init {
        ButterKnife.bind(this, iv)
    }

    fun setup(message: ForumMessage, topic: ForumTopic) {
        if (message.index == 1) {
            messageArea.cardElevation = dpToPixel(8f, messageArea.context)
        } else {
            messageArea.cardElevation = dpToPixel(2f, messageArea.context)
        }

        if (message.authorAvatarUrl != null) {
            messageAvatar.layoutVisibilityBool = true
            Glide.with(messageAvatar)
                .load(message.authorAvatarUrl)
                .apply(RequestOptions()
                    .centerInside()
                    .circleCrop())
                .into(messageAvatar)
        } else {
            messageAvatar.layoutVisibilityBool = false
        }

        messageAuthorName.text = message.author
        messageIndex.text = "#${message.index}"

        try {
            val creationDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(message.createdDate)!!
            messageDate.text = DateUtils.getRelativeTimeSpanString(creationDate.time)
            messageDate.setOnClickListener { it.showToast(message.createdDate) }
        } catch (ex: ParseException) {
            messageDate.text = message.createdDate
            messageDate.isClickable = false
        }

        messageBody.handleMarkdown(message.content)
        messageBody.customSelectionActionModeCallback = SelectionEnhancer(message, topic)

        // make text selectable
        // XXX: this is MAGIC: see https://stackoverflow.com/a/56224791/1696844
        messageBody.setTextIsSelectable(false)
        messageBody.measure(-1, -1)
        messageBody.setTextIsSelectable(true)

        messageMenu.setOnClickListener { configureContextMenu(it, message, topic) }
    }

    private fun configureContextMenu(anchor: View, message: ForumMessage, topic: ForumTopic) {
        val pmenu = PopupMenu(anchor.context, anchor)
        pmenu.inflate(R.menu.message_menu)
        pmenu.menu.iterator().forEach { mi -> DrawableCompat.setTint(mi.icon, anchor.resolveAttr(R.attr.colorOnSecondary)) }

        // share message permalink
        pmenu.menu.findItem(R.id.menu_message_share).setOnMenuItemClickListener {
            anchor.context.shareLink(message.link)
            true
        }

        // insert full message quote
        pmenu.menu.findItem(R.id.menu_message_quote).setOnMenuItemClickListener {
            val waitDialog = MaterialAlertDialogBuilder(anchor.context)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.loading)
                .create()

            GlobalScope.launch(Dispatchers.Main) {
                waitDialog.show()

                Network.perform(anchor.context,
                    networkAction = { Network.loadQuote(topic.id, message.id) },
                    uiAction = { quote -> openQuotedReply(topic, mapOf(AddMessageFragment.FULL_QUOTE_ARG to quote)) }
                )

                waitDialog.dismiss()
            }
            true
        }

        val helper = MenuPopupHelper(anchor.context, pmenu.menu as MenuBuilder, anchor)
        helper.setForceShowIcon(true)
        helper.show()
    }

    /**
     * open create new message fragment and insert quote
     */
    private fun openQuotedReply(topic: ForumTopic, params: Map<String, String>) {
        val messageAdd = AddMessageFragment().apply {
            arguments = Bundle().apply {
                putInt(AddMessageFragment.TOPIC_ID_ARG, topic.id)
                params.forEach(action = {entry ->  putString(entry.key, entry.value)})
            }
        }

        val activity = itemView.context as AppCompatActivity
        messageAdd.show(activity.supportFragmentManager, "showing add message fragment")
    }

    /**
     * Enhances selection of the text in the specified message.
     * Shows "Reply" button that opens [AddMessageFragment] with the selected text quoted.
     */
    inner class SelectionEnhancer(private val message: ForumMessage, private val topic: ForumTopic): ActionMode.Callback {

        private val textView = messageBody

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val text = textView.text.subSequence(textView.selectionStart, textView.selectionEnd)

            when(item.itemId) {
                R.id.menu_reply -> {
                    openQuotedReply(topic, mapOf(
                        AddMessageFragment.AUTHOR_ARG to message.author,
                        AddMessageFragment.MSGID_ARG to message.id.toString(),
                        AddMessageFragment.PARTIAL_QUOTE_ARG to text.toString()
                    ))

                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // don't show "Reply" option if topic is closed or we are not logged in
            if (!topic.isWritable) {
                return true
            }

            // we can write comments here, show "Reply" option
            mode.menuInflater.inflate(R.menu.content_selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onDestroyActionMode(mode: ActionMode) = Unit
    }
}