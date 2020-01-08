package com.kanedias.holywarsoo.markdown

import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.kanedias.holywarsoo.R
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import java.nio.charset.Charset
import java.security.MessageDigest


/**
 * @author Kanedias
 *
 * Created on 07.01.20
 */
class GlideGifSupportStore(txt: TextView): GlideImagesPlugin.GlideStore {

    private val requestManager = Glide.with(txt.context)
        .applyDefaultRequestOptions(
            RequestOptions()
                .centerInside()
                .override(txt.context.resources.displayMetrics.widthPixels, Target.SIZE_ORIGINAL)
                .transform(ScaleToDensity(txt.context))
                .placeholder(R.drawable.image)
                .error(R.drawable.image_broken))

    override fun cancel(target: Target<*>) = requestManager.clear(target)

    override fun load(drawable: AsyncDrawable) = requestManager.load(drawable.destination)


    /**
     * scales small images to match density of the screen. Mainly needed for smiley pictures.
     */
    class ScaleToDensity(ctx: Context): BitmapTransformation() {
        companion object {
            const val ID = "com.kanedias.holywarsoo.markdown.ScaleToDensity"
            val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
        }

        private val density = ctx.resources.displayMetrics.density

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            if (outHeight > 100) {
                return toTransform
            }

            val scaledWidth = (toTransform.width * density).toInt()
            val scaledHeight = (toTransform.height * density).toInt()
            return Bitmap.createScaledBitmap(toTransform, scaledWidth, scaledHeight, true)
        }

        override fun equals(other: Any?) = other is ScaleToDensity

        override fun hashCode() = ID.hashCode()

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(ID_BYTES)
        }
    }
}