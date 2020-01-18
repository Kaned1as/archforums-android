package com.kanedias.holywarsoo.misc

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Slide
import com.kanedias.holywarsoo.R


private val UNNEEDED_INT_CHARS = Regex("[,. ]")

var View.visibilityBool: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) { View.VISIBLE } else { View.INVISIBLE } }

var View.layoutVisibilityBool: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) { View.VISIBLE } else { View.GONE } }

/**
 * Show toast exactly under specified view
 *
 * @param view view at which toast should be located
 * @param text text of toast
 */
fun View.showToast(text: String) {
    val toast = Toast.makeText(this.context, text, Toast.LENGTH_SHORT)

    val location = IntArray(2)
    this.getLocationOnScreen(location)

    toast.setGravity(Gravity.TOP or Gravity.START, location[0] - 25, location[1] - 10)
    toast.show()
}

/**
 * Resolve attribute effectively
 * @param attr attribute, for example [R.attr.toolbarPopupOverrideStyle]
 * @return resolved reference
 */
fun View.resolveAttr(attr: Int): Int {
    val typedValue = TypedValue()
    this.context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

/**
 * Deduce what integer number should this string represent.
 * Gets rid of usual separator chars (we know it's integer).
 */
fun String.sanitizeInt(): Int {
    return this
        .replace(UNNEEDED_INT_CHARS, "")
        .replace(Regex("^-$"), "-1")
        .replace(Regex("-{2,}"), "-1")
        .toInt()
}

/**
 * Try to deduce what integer number should this string represent.
 * Gets rid of usual separator chars (we know it's integer).
 */
fun String?.trySanitizeInt(): Int? {
    if (this == null)
        return null

    return this
        .replace(UNNEEDED_INT_CHARS, "")
        .replace(Regex("^-$"), "")
        .replace(Regex("-{2,}"), "")
        .toIntOrNull()
}

/**
 * Overlays main view of the activity with the specified fragment.
 */
fun FragmentActivity.showFullscreenFragment(frag: Fragment) {
    frag.enterTransition = Slide(Gravity.END)
    frag.exitTransition = Slide(Gravity.START)

    supportFragmentManager.beginTransaction()
        .addToBackStack("Showing fragment: $frag")
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .add(R.id.main_area, frag)
        .commit()
}