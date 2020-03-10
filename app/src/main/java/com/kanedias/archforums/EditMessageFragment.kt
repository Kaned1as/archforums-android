package com.kanedias.archforums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.kanedias.archforums.database.entities.OfflineDraft
import com.kanedias.archforums.misc.layoutVisibilityBool
import com.kanedias.archforums.service.Database
import com.kanedias.archforums.service.Network
import com.kanedias.archforums.service.SpanCache
import kotlinx.coroutines.launch
import java.util.*

/**
 * Fragment responsible for editing a message, be it topic starting message or ordinary one.
 *
 * @author Kanedias
 *
 * Created on 2020-03-11
 */
class EditMessageFragment: EditorFragment() {

    companion object {
        const val DB_CONTEXT_PREFIX = "editmessage"

        /**
         * The message id being edited
         */
        const val EDIT_MESSAGE_ID_ARG = "EDIT_MESSAGE_ID_ARG"
    }

    @BindView(R.id.source_subject_helper)
    lateinit var subjectHelperLayout: TextInputLayout

    @BindView(R.id.source_subject)
    lateinit var subjectInput: EditText

    @BindView(R.id.main_post_area)
    lateinit var editorArea: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_message, container, false)
        ButterKnife.bind(this, view)

        editor = EditorViews(this, editorArea)
        lifecycleScope.launch {
            handleEdit()
            handleDraft()
        }

        return view
    }

    private suspend fun handleEdit() {
        val editId = requireArguments().getInt(EDIT_MESSAGE_ID_ARG)

        val waitDialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.please_wait)
            .setMessage(R.string.loading)
            .create()

        // load message asynchronously while showing wait dialog
        // dismiss if we couldn't edit
        waitDialog.show()

        Network.perform(
            networkAction = { Network.loadEditPost(editId) },
            uiAction = { message ->
                if (!message.subject.isNullOrEmpty()) {
                    subjectHelperLayout.layoutVisibilityBool = true
                    subjectInput.setText(message.subject)
                }
                editor.contentInput.setText(message.content)
            },
            exceptionAction = {
                Network.reportErrors(context, it)
                dismiss()
            }
        )

        waitDialog.dismiss()
    }

    private fun handleDraft() {
        val messageId = requireArguments().getInt(EDIT_MESSAGE_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${messageId}"

        // if draft exists with this key, fill content with it
        Database.draftDao().getByKey(contextKey)?.let {
            editor.contentInput.setText(it.content)
            editor.contentInput.setSelection(editor.contentInput.length())
        }

        // delay saving text a bit so database won't be spammed with it
        editor.contentInput.addTextChangedListener { text ->
            val action = {
                val draft = OfflineDraft(createdAt = Date(), ctxKey = contextKey, content = text.toString())
                Database.draftDao().insertDraft(draft)
            }
            editor.contentInput.removeCallbacks(action)
            editor.contentInput.postDelayed(action, 1500)
        }
    }

    @OnClick(R.id.message_cancel)
    fun cancel() {
        dialog?.cancel()
    }

    @OnClick(R.id.message_submit)
    fun submit() {
        val messageId = requireArguments().getInt(EDIT_MESSAGE_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${messageId}"

        val waitDialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.please_wait)
            .setMessage(R.string.submitting)
            .create()

        val frgPredicate = { it: Fragment -> it is ContentFragment }
        val curFrg = requireFragmentManager().fragments.reversed().find(frgPredicate) as TopicContentFragment?
        val edited = Network.EditMessageDesc(
            subject = subjectInput.text.toString(),
            content = editor.contentInput.text.toString()
        )

        lifecycleScope.launch {
            waitDialog.show()

            Network.perform(
                networkAction = { Network.editMessage(messageId, edited) },
                uiAction = { link ->
                    // delete this message from cache, or refresh
                    // will yield nothing
                    SpanCache.removeMessageId(messageId)

                    // delete draft of this message, prevent reinsertion
                    // should be race-free since it's in the same thread as this one (Main UI thread)
                    editor.contentInput.handler?.removeCallbacksAndMessages(contextKey)
                    Database.draftDao().deleteByKey(contextKey)

                    // refresh parent fragment
                    curFrg?.arguments?.putString(TopicContentFragment.URL_ARG, link.toString())
                    curFrg?.refreshContent()

                    dismiss()
                })

            waitDialog.dismiss()
        }
    }
}