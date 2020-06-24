package com.kanedias.archforums.service

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kanedias.archforums.markdown.GlideGifSupportStore
import okhttp3.HttpUrl
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Tracks available smilies on a service.
 * Smilies are specific for each forum instance.
 *
 * @author Kanedias
 *
 * Created on 21.06.20
 */
object SmiliesCache {

    const val SMILIES_PREFIX = "/img/smilies"

    private val cache = HashMap<String, Drawable>(512)

    fun init(ctx: Context) {
        val smiliesTxt = ctx.assets.open("smile-mappings.json").readBytes().toString(StandardCharsets.UTF_8)
        val smiliesJson = JSONObject(smiliesTxt)
        for (key in smiliesJson.keys()) {
            val smilieUrl = Network.resolve("$SMILIES_PREFIX/${smiliesJson.getString(key)}") ?: continue
            putSmilie(ctx, key, smilieUrl)
        }
    }

    fun putSmilie(ctx: Context, key: String, address: HttpUrl) {
        Glide.with(ctx)
            .asDrawable()
            .transform(GlideGifSupportStore.ScaleToDensity(ctx))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(address.toString())
            .into(object: CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    cache.put(key, resource)
                }

            })
    }

    fun containsKey(key: String) = cache.containsKey(key)

    fun getSmilie(key: String) = cache[key]

    fun getAllSmilies() = cache.clone() as Map<String, Drawable>
}