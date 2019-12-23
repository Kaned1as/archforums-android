package com.kanedias.holywarsoo.misc

import android.view.View

private val UNNEEDED_INT_CHARS = Regex("[,. ]")

var View.visibilityBool: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) { View.VISIBLE } else { View.INVISIBLE } }

fun String.sanitizeInt(): Int {
    return this
        .replace(UNNEEDED_INT_CHARS, "")
        .replace("-", "0")
        .toInt()
}

fun String.trySanitizeInt(): Int? {
    return this
        .replace(UNNEEDED_INT_CHARS, "")
        .replace("-", "0")
        .toIntOrNull()
}