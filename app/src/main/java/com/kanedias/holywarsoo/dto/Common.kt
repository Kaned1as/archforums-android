package com.kanedias.holywarsoo.dto

import okhttp3.HttpUrl
import java.io.Serializable

/**
 * @author Kanedias
 *
 * Created on 21.12.19
 */

data class NamedLink(
    val name: String,
    val url: HttpUrl
) : Serializable