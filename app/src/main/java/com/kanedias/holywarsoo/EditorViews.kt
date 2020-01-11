package com.kanedias.holywarsoo

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.*

/**
 * Fragment to hold all editing-related functions in all edit views where possible.
 *
 * @author Kanedias
 *
 * Created on 07.04.18
 */
class EditorViews(private val parent: Fragment, private val iv: View) {

    companion object {
        const val ACTIVITY_REQUEST_IMAGE_UPLOAD = 0
        const val PERMISSION_REQUEST_STORAGE_FOR_IMAGE_UPLOAD = 0
    }

    @BindView(R.id.source_text)
    lateinit var contentInput: EditText

    @BindView(R.id.edit_insert_from_clipboard)
    lateinit var clipboardSwitch: CheckBox

    @BindView(R.id.edit_quick_image)
    lateinit var imageUpload: ImageView

    @BindView(R.id.edit_quick_button_area)
    lateinit var buttonArea: GridLayout

    init {
        ButterKnife.bind(this, iv)

        // start editing content right away
        contentInput.requestFocus()
    }

    /**
     * Handler of all small editing buttons above content input.
     */
    @OnClick(
            R.id.edit_quick_bold, R.id.edit_quick_italic, R.id.edit_quick_underlined, R.id.edit_quick_strikethrough,
            R.id.edit_quick_code, R.id.edit_quick_quote, R.id.edit_quick_number_list, R.id.edit_quick_bullet_list,
            R.id.edit_quick_link, R.id.edit_quick_more
    )
    fun editSelection(clicked: View) {
        val clipboard = iv.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var paste = if (clipboardSwitch.isChecked && clipboard.hasPrimaryClip() && clipboard.primaryClip!!.itemCount > 0) {
            clipboard.primaryClip!!.getItemAt(0).text.toString()
        } else {
            ""
        }

        // check whether we have text selected in content input
        if (paste.isEmpty() && contentInput.hasSelection()) {
            // delete selection
            paste = contentInput.text.substring(contentInput.selectionStart until contentInput.selectionEnd)
            contentInput.text.delete(contentInput.selectionStart, contentInput.selectionEnd)
        }

        val moreTxt = iv.context.getString(R.string.more_tag_default)

        when (clicked.id) {
            R.id.edit_quick_bold -> insertInCursorPosition("[b]", paste, "[/b]")
            R.id.edit_quick_italic -> insertInCursorPosition( "[i]", paste, "[/i]")
            R.id.edit_quick_underlined -> insertInCursorPosition("[u]", paste, "[/u]")
            R.id.edit_quick_strikethrough -> insertInCursorPosition("[s]", paste, "[/s]")
            R.id.edit_quick_code -> insertInCursorPosition("[code]", paste, "[/code]")
            R.id.edit_quick_quote -> insertInCursorPosition("[quote]", paste, "[/quote]")
            R.id.edit_quick_number_list -> insertInCursorPosition("[list]", paste, "[/list]")
            R.id.edit_quick_bullet_list -> insertInCursorPosition("[*]", paste, "[/*]")
            R.id.edit_quick_link -> insertInCursorPosition("[url]", paste, "[/url]")
            R.id.edit_quick_image -> insertInCursorPosition("[img]", paste, "[/img]")
            R.id.edit_quick_more -> insertInCursorPosition("[spoiler=$moreTxt]", paste, "[/spoiler]")
        }

        clipboardSwitch.isChecked = false
    }

    /**
     * Image upload button requires special handling
     */
    @OnClick(R.id.edit_quick_image)
    fun uploadImage(clicked: View) {
        // sometimes we need SD-card access to load the image
        if (ContextCompat.checkSelfPermission(iv.context, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            parent.requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE_FOR_IMAGE_UPLOAD)
            return
        }

        if (clipboardSwitch.isChecked) {
            // delegate to just paste image link from clipboard
            editSelection(clicked)
            return
        }

        // not from clipboard, show upload dialog
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            val chooser = Intent.createChooser(intent, iv.context.getString(R.string.select_image_to_upload))
            parent.startActivityForResult(chooser, ACTIVITY_REQUEST_IMAGE_UPLOAD)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(iv.context, iv.context.getString(R.string.no_file_manager_found), Toast.LENGTH_SHORT).show()
        }
    }

    fun requestImageUpload(intent: Intent?) {
        if (intent?.data == null)
            return

        val stream = iv.context?.contentResolver?.openInputStream(intent.data as Uri) ?: return

        val dialog = MaterialAlertDialogBuilder(iv.context)
                .setTitle(R.string.please_wait)
                .setMessage(R.string.uploading)
                .create()

        GlobalScope.launch(Dispatchers.Main) {
            dialog.show()

            try {
                val link = withContext(Dispatchers.IO) { Network.uploadImage(stream.readBytes()) }
                insertInCursorPosition("[img]", link, "[/img]")
            } catch (ex: Exception) {
                Network.reportErrors(iv.context, ex)
            }

            dialog.dismiss()
        }
    }

    /**
     * Helper function for inserting quick snippets of markup into the various parts of edited text
     * @param prefix prefix preceding content.
     *          This is most likely non-empty. Cursor is positioned after it in all cases.
     * @param what content to insert.
     *          If it's empty and [suffix] is not, cursor will be positioned here
     * @param suffix suffix after content. Can be empty fairly often. Cursor will be placed after it if [what] is
     *          not empty.
     */
    private fun insertInCursorPosition(prefix: String, what: String, suffix: String = "") {
        var cursorPos = contentInput.selectionStart
        if (cursorPos == -1)
            cursorPos = contentInput.text.length

        val beforeCursor = contentInput.text.substring(0, cursorPos)
        val afterCursor = contentInput.text.substring(cursorPos, contentInput.text.length)

        val beforeCursorWithPrefix = beforeCursor + prefix
        val suffixWithAfterCursor = suffix + afterCursor
        val result = beforeCursorWithPrefix + what + suffixWithAfterCursor
        contentInput.setText(result)

        contentInput.setSelection(cursorPos + prefix.length, cursorPos + prefix.length + what.length)
    }
}