package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.kanedias.holywarsoo.database.entities.OfflineDraft
import com.kanedias.holywarsoo.misc.layoutVisibilityBool
import com.kanedias.holywarsoo.misc.showFullscreenFragment
import com.kanedias.holywarsoo.service.Database
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.launch
import java.util.*

/**
 * Fragment responsible for created a new topic in specified forum.
 * Forum id **must** be sent as an argument.
 *
 * @author Kanedias
 *
 * Created on 2019-12-29
 */
class AddTopicFragment: EditorFragment() {

    companion object {
        const val DB_CONTEXT_PREFIX = "newtopic"

        /**
         * Required, the forum in which topic should be created
         */
        const val FORUM_ID_ARG = "FORUM_ID_ARG"
    }

    @BindView(R.id.main_post_area)
    lateinit var editorArea: LinearLayout

    @BindView(R.id.source_subject_helper)
    lateinit var subjectHelperLayout: TextInputLayout

    @BindView(R.id.source_subject)
    lateinit var subjectInput: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_message, container, false)
        ButterKnife.bind(this, view)

        editor = EditorViews(this, editorArea)
        subjectInput.requestFocus()
        subjectHelperLayout.layoutVisibilityBool = true

        handleDraft()

        return view
    }

    private fun handleDraft() {
        val forumId = requireArguments().getInt(FORUM_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${forumId}"

        // if draft exists with this key, fill content with it
        Database.draftDao().getByKey(contextKey)?.let {
            subjectInput.setText(it.title)
            editor.contentInput.setText(it.content)
            editor.contentInput.setSelection(editor.contentInput.length())
        }

        // delay saving text a bit so database won't be spammed with it
        editor.contentInput.addTextChangedListener { text ->
            val action = {
                val draft = OfflineDraft(
                    createdAt = Date(),
                    ctxKey = contextKey,
                    title = subjectInput.text.toString(),
                    content = text.toString())
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
        val forumId = requireArguments().getInt(FORUM_ID_ARG)
        val contextKey = "${DB_CONTEXT_PREFIX}-${forumId}"

        val waitDialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.please_wait)
            .setMessage(R.string.submitting)
            .create()

        lifecycleScope.launch {
            waitDialog.show()

            Network.perform(
                networkAction = { Network.postTopic(forumId, subjectInput.text.toString(), editor.contentInput.text.toString()) },
                uiAction = { link ->
                    // delete draft of this message, prevent reinsertion
                    // should be race-free since it's in the same thread as this one (Main UI thread)
                    editor.contentInput.handler?.removeCallbacksAndMessages(contextKey)
                    Database.draftDao().deleteByKey(contextKey)

                    // open new topic fragment
                    val fragment = TopicContentFragment().apply {
                        arguments = Bundle().apply {
                            putString(TopicContentFragment.URL_ARG, link.toString())
                        }
                    }
                    activity?.showFullscreenFragment(fragment)

                    dismiss()
                })

            waitDialog.dismiss()
        }
    }
}