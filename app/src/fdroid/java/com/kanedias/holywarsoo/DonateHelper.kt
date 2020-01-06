package com.kanedias.holywarsoo

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 * Flavor-specific donation helper class. This manages menu option "Donate" in the main activity.
 *
 * @author Kanedias
 *
 * Created on 10.04.18
 */
class DonateHelper(private val activity: AppCompatActivity) {

    fun donate() {
        val options = arrayOf("Paypal", "Patreon", "Liberapay")
        MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.donate)
            .setItems(options) { dialog: DialogInterface, pos: Int -> when(pos) {
                0 -> openLink("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6CFFYXFT6QT46")
                1 -> openLink("https://www.patreon.com/kanedias")
                2 -> openLink("https://liberapay.com/Kanedias")
                else -> {}
            }}
            .show()
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

}