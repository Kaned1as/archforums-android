package com.kanedias.holywarsoo

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.iterator
import butterknife.BindView
import com.kanedias.holywarsoo.dto.ForumMessage
import com.kanedias.holywarsoo.dto.NavigationScope
import com.kanedias.holywarsoo.misc.resolveAttr

/**
 * View holder for search messages. Search messages, in addition to usual content,
 * also contain navigation links to various places they can be located in.
 *
 * @see SearchMessagesContentFragment
 *
 * @author Kanedias
 *
 * Created on 20-01-26
 */
class SearchMessageViewHolder(iv: View): MessageViewHolder(iv) {

    @BindView(R.id.message_navlink_to_forum)
    lateinit var navlinkToForum: TextView

    @BindView(R.id.message_navlink_to_topic)
    lateinit var navlinkToTopic: TextView

    @BindView(R.id.message_navlink_to_message)
    lateinit var navlinkToMessage: TextView

    override fun setup(message: ForumMessage) {
        // this forum message *must* have navigation links
        super.setup(message)

        val ctx = itemView.context

        val toForumSuffix = ctx.getString(R.string.navigate_to_forum)
        val toForumMessage = message.navigationLinks.getValue(NavigationScope.FORUM).first
        val toForumLink = message.navigationLinks.getValue(NavigationScope.FORUM).second
        navlinkToForum.text = "$toForumSuffix $toForumMessage"
        navlinkToForum.setOnClickListener { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(toForumLink))) }

        val toTopicSuffix = ctx.getString(R.string.navigate_to_topic)
        val toTopicMessage = message.navigationLinks.getValue(NavigationScope.TOPIC).first
        val toTopicLink = message.navigationLinks.getValue(NavigationScope.TOPIC).second
        navlinkToTopic.text = "$toTopicSuffix $toTopicMessage"
        navlinkToTopic.setOnClickListener { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(toTopicLink))) }

        val toMessageSuffix = ctx.getString(R.string.navigate_to_message)
        val toMessageMessage = message.navigationLinks.getValue(NavigationScope.MESSAGE).first
        val toMessageLink = message.navigationLinks.getValue(NavigationScope.MESSAGE).second
        navlinkToMessage.text = "$toMessageSuffix $toMessageMessage"
        navlinkToMessage.setOnClickListener { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(toMessageLink))) }

        messageMenu.setOnClickListener { configureContextMenu(it, message) }
    }

    private fun configureContextMenu(anchor: View, message: ForumMessage) {
        val pmenu = PopupMenu(anchor.context, anchor)
        pmenu.inflate(R.menu.message_menu)
        pmenu.menu.iterator().forEach { mi -> DrawableCompat.setTint(mi.icon, anchor.resolveAttr(R.attr.colorOnSecondary)) }

        // we have nowhere to reply here, we're in the search page
        pmenu.menu.findItem(R.id.menu_message_quote).setVisible(false)

        configureContextMenu(pmenu, anchor, message)

        val helper = MenuPopupHelper(anchor.context, pmenu.menu as MenuBuilder, anchor)
        helper.setForceShowIcon(true)
        helper.show()
    }
}