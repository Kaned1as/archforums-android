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

/**
 * @author Kanedias
 *
 * Created on 29.12.19
 */
class AddMessageFragment: EditorFragment() {

    companion object {
        const val TOPIC_ARG = "TOPIC_ARG"
    }

    @BindView(R.id.edit_area)
    lateinit var editorArea: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_message, container, false)
        ButterKnife.bind(this, view)

        editor = EditorViews(this, editorArea)

        return view
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