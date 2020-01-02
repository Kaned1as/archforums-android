package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.text.Typography.quote

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
        const val TOPIC_ARG = "TOPIC_ARG"
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
        handleMisc()

        return view
    }

    /**
     * Handles miscellaneous conditions, such as:
     * * This fragment was shown due to click on author's nickname
     * * This fragment was shown due to quoting
     *
     */
    private fun handleMisc() {
        // handle click on reply in text selection menu
        val authorName = arguments?.getString(AUTHOR_ARG)
        val quotedText = arguments?.getString(QUOTE_ARG)
        val quotedId = arguments?.getString(MSGID_ARG)

        if (authorName.isNullOrEmpty() || quotedText.isNullOrEmpty() || quotedId.isNullOrEmpty()) {
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
        val waitDialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.please_wait)
            .setMessage(R.string.submitting)
            .create()

        val frgPredicate = { it: Fragment -> it is ContentFragment }
        val curFrg = requireFragmentManager().fragments.reversed().find(frgPredicate) as ContentFragment?

        lifecycleScope.launch {
            waitDialog.show()

            try {
                val topic = requireArguments().getSerializable(TOPIC_ARG) as ForumTopic
                withContext(Dispatchers.IO) { Network.postMessage(topic, editor.contentInput.text.toString()) }
                curFrg?.refreshContent()
                dismiss()
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            waitDialog.dismiss()
        }
    }
}