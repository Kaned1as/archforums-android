package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.postDelayed
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanedias.holywarsoo.database.entities.OfflineDraft
import com.kanedias.holywarsoo.service.Database
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

/**
 * Fragment responsible for adding a new message to the specified topic.
 * Topic **must** be sent as an argument.
 *
 * @author Kanedias
 *
 * Created on 29.12.19
 */
class AddMessageFragment: EditorFragment() {

    companion object {
        const val DB_CONTEXT_PREFIX = "newmessage-topic"
        const val TOPIC_ID_ARG = "TOPIC_ID_ARG"
        const val AUTHOR_ARG = "AUTHOR_ARG"
        const val MSGID_ARG = "MSGID_ARG"
        const val QUOTE_ARG = "QUOTE_ARG"
    }

    @BindView(R.id.edit_area)
    lateinit var editorArea: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_message, container, false)
        ButterKnife.bind(this, view)

        editor = EditorViews(this, editorArea)
        handleDraft()
        handleMisc()

        return view
    }

    private fun handleDraft() {
        val topicId = requireArguments().getInt(TOPIC_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${topicId}"

        // if draft exists with this key, fill content with it
        Database.draftDao().getByKey(contextKey)?.let {
            editor.contentInput.setText(it.content)
            editor.contentInput.setSelection(editor.contentInput.length())
        }

        // delay saving text a bit so database won't be spammed with it
        editor.contentInput.addTextChangedListener { text ->
            editor.contentInput.handler?.removeCallbacksAndMessages(contextKey)
            editor.contentInput.handler?.postDelayed(1500, contextKey, action = {
                val draft = OfflineDraft(createdAt = Date(), ctxKey = contextKey, content = text.toString())
                Database.draftDao().insertDraft(draft)
            })
        }
    }

    /**
     * Handles miscellaneous conditions, such as:
     * * This fragment was shown due to click on author's nickname
     * * This fragment was shown due to quoting
     *
     */
    private fun handleMisc() {
        // handle click on reply in text selection menu
        val authorName = requireArguments().getString(AUTHOR_ARG)
        val quotedText = requireArguments().getString(QUOTE_ARG)
        val quotedId = requireArguments().getString(MSGID_ARG)

        if (authorName.isNullOrEmpty() || quotedText.isNullOrEmpty() || quotedId.isNullOrEmpty()) {
            // not provided, add nothing
            return
        }

        var quoteText = "[quote=\"$authorName\", post=${quotedId}]\n" +
                "$quotedText\n" +
                "[/quote]\n\n"

        if (editor.contentInput.text.isNotEmpty()) {
            quoteText = "${editor.contentInput.text}\n" + quoteText
        }

        editor.contentInput.setText(quoteText)
        editor.contentInput.setSelection(quoteText.length)
    }

    @OnClick(R.id.message_cancel)
    fun cancel() {
        dialog?.cancel()
    }

    @OnClick(R.id.message_submit)
    fun submit() {
        val topicId = requireArguments().getInt(TOPIC_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${topicId}"

        val waitDialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.please_wait)
            .setMessage(R.string.submitting)
            .create()

        val frgPredicate = { it: Fragment -> it is ContentFragment }
        val curFrg = requireFragmentManager().fragments.reversed().find(frgPredicate) as TopicContentFragment?

        lifecycleScope.launch {
            waitDialog.show()

            try {
                val link = withContext(Dispatchers.IO) {
                    Network.postMessage(topicId, editor.contentInput.text.toString())
                }

                // delete draft of this message, prevent reinsertion
                // should be race-free since it's in the same thread as this one (Main UI thread)
                editor.contentInput.handler?.removeCallbacksAndMessages(contextKey)
                Database.draftDao().deleteByKey(contextKey)

                // refresh parent fragment
                curFrg?.arguments?.putString(TopicContentFragment.URL_ARG, link.toString())
                curFrg?.refreshContent()

                dismiss()
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            waitDialog.dismiss()
        }
    }
}