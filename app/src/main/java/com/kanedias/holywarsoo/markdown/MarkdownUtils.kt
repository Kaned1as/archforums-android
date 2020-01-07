package com.kanedias.holywarsoo.markdown

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.bumptech.glide.request.transition.Transition
import com.kanedias.holywarsoo.BuildConfig
import com.kanedias.holywarsoo.R
import com.kanedias.holywarsoo.misc.dpToPixel
import com.kanedias.holywarsoo.service.Network
import com.kanedias.html2md.Html2Markdown
import com.stfalcon.imageviewer.StfalconImageViewer
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.BlockQuoteSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.AsyncDrawableSpan
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.utils.NoCopySpannableFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

/**
 * Get markdown setup from context.
 *
 * This is stripped-down version for use in notifications and other
 * cases where user interaction is not needed.
 *
 * @param ctx context to initialize from
 */
fun mdRendererFrom(ctx: Context): Markwon {
    return Markwon.builder(ctx)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .build()
}

/**
 * Get commonly-used markdown setup for text views
 */
fun mdRendererFrom(txt: TextView): Markwon {
    return Markwon.builder(txt.context)
        .usePlugin(object: AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.blockMargin(dpToPixel(16f, txt.context).toInt())
            }
        })
        .usePlugin(HtmlPlugin.create()
            .addHandler(DetailsTagHandler()))
        .usePlugin(GlideImagesPlugin.create(
            Glide.with(txt.context)
                .applyDefaultRequestOptions(RequestOptions()
                    .centerInside()
                    .override(txt.context.resources.displayMetrics.widthPixels, SIZE_ORIGINAL)
                    .transform(ScaleToDensity(txt.context))
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image_broken))))
        .usePlugin(StrikethroughPlugin.create())
        .build()
}

fun mdThemeFrom(txt: TextView): MarkwonTheme {
    return MarkwonTheme.builderWithDefaults(txt.context)
        .blockMargin(dpToPixel(16f, txt.context).toInt())
        .build()
}

/**
 * Perform all necessary steps to view Markdown in this text view.
 * Parses input with html2md library and converts resulting markdown to spanned string.
 * @param html input markdown to show
 */
infix fun TextView.handleMarkdown(html: String) {
    val label = this
    label.setSpannableFactory(NoCopySpannableFactory())

    GlobalScope.launch(Dispatchers.Main) {
        // this is computation-intensive task, better do it smoothly
        val span = withContext(Dispatchers.IO) {
            val mdContent = Html2Markdown().parse(html)
            val spanned = mdRendererFrom(label).toMarkdown(mdContent) as SpannableStringBuilder
            postProcessSpans(spanned, label)

            spanned
        }

        label.text = span

        // FIXME: see https://github.com/noties/Markwon/issues/120
        label.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {

            }

            override fun onViewAttachedToWindow(v: View?) {
                AsyncDrawableScheduler.schedule(label)
            }

        })

        AsyncDrawableScheduler.schedule(label)
    }
}

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


/**
 * Post-process spans like spoilers or image loading
 * @param spanned editable spannable to change
 * @param view resulting text view to accept the modified spanned string
 */
fun postProcessSpans(spanned: SpannableStringBuilder, view: TextView) {
    postProcessDrawables(spanned, view)
    postProcessMore(spanned, view)
}

/**
 * Post-process drawables, so you can click on them to see them in full-screen
 * @param spanned editable spannable to change
 * @param view resulting text view to accept the modified spanned string
 */
fun postProcessDrawables(spanned: SpannableStringBuilder, view: TextView) {
    val imgSpans = spanned.getSpans(0, spanned.length, AsyncDrawableSpan::class.java)
    imgSpans.sortBy { spanned.getSpanStart(it) }

    for (img in imgSpans) {
        val start = spanned.getSpanStart(img)
        val end = spanned.getSpanEnd(img)
        val spansToWrap = spanned.getSpans(start, end, CharacterStyle::class.java)
        if (spansToWrap.any { it is ClickableSpan }) {
            // the image is already clickable, we can't replace it
            continue
        }

        val wrapperClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val index = imgSpans.indexOf(img)
                if (index == -1) {
                    // something modified spannable in a way image is no longer here
                    return
                }

                val overlay = ImageShowOverlay(view.context)
                overlay.update(imgSpans[index])

                StfalconImageViewer.Builder<AsyncDrawableSpan>(view.context, imgSpans) { view, span ->
                    val resolved = Network.resolve(span.drawable.destination) ?: return@Builder
                    Glide.with(view).load(resolved.toString()).into(view)
                }
                    .withOverlayView(overlay)
                    .withStartPosition(index)
                    .withImageChangeListener { position -> overlay.update(imgSpans[position])}
                    .allowSwipeToDismiss(true)
                    .show()
            }
        }

        spanned.setSpan(wrapperClick, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

/**
 * Post-process MORE statements in the text. They act like `<spoiler>` or `<cut>` tag in some websites
 * @param spanned text to be modified to cut out MORE tags and insert replacements instead of them
 * @param view resulting text view to accept the modified spanned string
 */
fun postProcessMore(spanned: SpannableStringBuilder, view: TextView) {
    val spans = spanned.getSpans(0, spanned.length, DetailsParsingSpan::class.java)
    spans.sortBy { spanned.getSpanStart(it) }

    // if we have no details, proceed as usual (single text-view)
    if (spans.isNullOrEmpty()) {
        // no details
        return
    }

    for (span in spans) {
        val startIdx = spanned.getSpanStart(span)
        val endIdx = spanned.getSpanEnd(span)

        val summaryStartIdx = spanned.getSpanStart(span.summary)
        val summaryEndIdx = spanned.getSpanEnd(span.summary)

        // details tags can be nested, skip them if they were hidden
        if (startIdx == -1 || endIdx == -1) {
            continue
        }

        // replace text inside spoiler tag with just spoiler summary that is clickable
        val summaryText = when (span.state) {
            DetailsSpanState.CLOSED -> "${span.summary.text} ▼\n\n"
            DetailsSpanState.OPENED  -> "${span.summary.text} ▲\n\n"
            else -> ""
        }

        when (span.state) {

            DetailsSpanState.CLOSED -> {
                span.state = DetailsSpanState.DORMANT
                spanned.removeSpan(span.summary) // will be added later

                // spoiler tag must be closed, all the content under it must be hidden

                // retrieve content under spoiler tag and hide it
                // if it is shown, it should be put in blockquote to distinguish it from text before and after
                val innerSpanned = spanned.subSequence(summaryEndIdx, endIdx) as SpannableStringBuilder
                spanned.replace(summaryStartIdx, endIdx, summaryText)
                spanned.setSpan(span.summary, startIdx, startIdx + summaryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // expand text on click
                val wrapper = object : ClickableSpan() {

                    // replace wrappers with real previous spans on click
                    override fun onClick(widget: View) {
                        span.state = DetailsSpanState.OPENED

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            // on devices with api < 28 span watcher update sometimes causes IndexOutOfBoundsException
                            // see https://issuetracker.google.com/issues/67924069
                            spanned.getSpans(0, spanned.length, SpanWatcher::class.java).forEach { spanned.removeSpan(it) }
                        }

                        val start = spanned.getSpanStart(this)
                        val end = spanned.getSpanEnd(this)

                        spanned.removeSpan(this)
                        spanned.insert(end, innerSpanned)

                        // make details span cover all expanded text
                        spanned.removeSpan(span)
                        spanned.setSpan(span, start, end + innerSpanned.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        // edge-case: if the span around this text is now too short, expand it as well
                        spanned.getSpans(end, end, Any::class.java)
                            .filter { spanned.getSpanEnd(it) == end }
                            .forEach {
                                if (it is DetailsSummarySpan) {
                                    // don't expand summaries, they are meant to end there
                                    return@forEach
                                }

                                val bqStart = spanned.getSpanStart(it)
                                spanned.removeSpan(it)
                                spanned.setSpan(it, bqStart, end + innerSpanned.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }

                        postProcessMore(spanned, view)

                        view.text = spanned
                        AsyncDrawableScheduler.schedule(view)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ds.linkColor
                    }
                }
                spanned.setSpan(wrapper, startIdx, startIdx + summaryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            DetailsSpanState.OPENED -> {
                span.state = DetailsSpanState.DORMANT

                // put the hidden text into blockquote if needed
                var bq = spanned.getSpans(summaryEndIdx, endIdx, BlockQuoteSpan::class.java)
                    .firstOrNull { spanned.getSpanStart(it) == summaryEndIdx && spanned.getSpanEnd(it) == endIdx }
                if (bq == null) {
                    bq = BlockQuoteSpan(mdThemeFrom(view))
                    spanned.setSpan(bq, summaryEndIdx, endIdx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // content under spoiler tag is shown, but should be hidden again on click
                // change summary text to opened variant
                spanned.replace(summaryStartIdx, summaryEndIdx, summaryText)
                spanned.setSpan(span.summary, startIdx, startIdx + summaryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val wrapper = object : ClickableSpan() {

                    // hide text again on click
                    override fun onClick(widget: View) {
                        span.state = DetailsSpanState.CLOSED

                        spanned.removeSpan(this)

                        postProcessMore(spanned, view)

                        view.text = spanned
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ds.linkColor
                    }
                }
                spanned.setSpan(wrapper, startIdx, startIdx + summaryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            DetailsSpanState.DORMANT -> {
                // this state is present so that details spans that were already processed won't be processed again
                // nothing should be done
            }
        }
    }
}

/**
 * Version without parsing html
 * @see handleMarkdown
 */
infix fun TextView.handleMarkdownRaw(markdown: String) {
    val spanned = mdRendererFrom(this).toMarkdown(markdown) as SpannableStringBuilder
    postProcessSpans(spanned, this)

    this.text = spanned
}

/**
 * Class responsible for showing "Share" and "Download" buttons when viewing images in full-screen.
 */
class ImageShowOverlay(ctx: Context,
                       attrs: AttributeSet? = null,
                       defStyleAttr: Int = 0) : FrameLayout(ctx, attrs, defStyleAttr) {

    @BindView(R.id.overlay_download)
    lateinit var download: ImageView

    @BindView(R.id.overlay_share)
    lateinit var share: ImageView

    init {
        View.inflate(ctx, R.layout.view_image_overlay, this)
        ButterKnife.bind(this)
    }

    fun update(span: AsyncDrawableSpan) {
        val resolved = Network.resolve(span.drawable.destination) ?: return

        // share button: share the image using file provider
        share.setOnClickListener {
            Glide.with(it).asFile().load(resolved.toString()).into(object: SimpleTarget<File>() {

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val shareUri = saveToShared(resource)

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image_using)))
                }

            })
        }

        // download button: download the image to Download folder on internal SD
        download.setOnClickListener {
            val activity = context as? Activity ?: return@setOnClickListener

            // request SD write permissions if we don't have it already
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                return@setOnClickListener
            }

            Toast.makeText(context, R.string.downloading_image, Toast.LENGTH_SHORT).show()
            Glide.with(it).asFile().load(resolved.toString()).into(object: SimpleTarget<File>() {

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(getMimeTypeOfFile(resource))
                    var name = resolved.pathSegments().last()
                    if (ext!= null && !name.endsWith(ext)) {
                        name += ".$ext"
                    }
                    val downloadedFile = File(downloads, name)
                    downloadedFile.writeBytes(resource.readBytes())

                    val report = context.getString(R.string.image_saved_as) + " ${downloadedFile.absolutePath}"
                    Toast.makeText(context, report, Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    /**
     * Decode the file as bitmap and retrieve its mime type, if it's an image
     * @param file input file to parse
     * @return string-represented mime type of this file
     */
    private fun getMimeTypeOfFile(file: File): String? {
        val opt = BitmapFactory.Options()
        opt.inJustDecodeBounds = true

        FileInputStream(file).use {
            BitmapFactory.decodeStream(it, null, opt)
        }

        return opt.outMimeType
    }

    /**
     * Save image file to location that is shared from xml/shared_paths.
     * After that it's possible to share this file to other applications
     * using [FileProvider].
     *
     * @param image image file to save
     * @return Uri that file-provider returns, or null if unable to
     */
    private fun saveToShared(image: File) : Uri? {
        try {
            val sharedImgs = File(context.cacheDir, "shared_images")
            if (!sharedImgs.exists() && !sharedImgs.mkdir()) {
                Log.e("Fair/Markdown", "Couldn't create dir for shared imgs! Path: $sharedImgs")
                return null
            }

            // cleanup old images
            for (oldImg in sharedImgs.listFiles()) {
                if (!oldImg.delete()) {
                    Log.w("Fair/Markdown", "Couldn't delete old image file! Path $oldImg")
                }
            }

            val imgTmpFile = File(sharedImgs, UUID.randomUUID().toString())
            imgTmpFile.writeBytes(image.readBytes())

            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", imgTmpFile)
        } catch (e: IOException) {
            Log.d("Fair/Markdown", "IOException while trying to write file for sharing", e)
        }

        return null
    }
}

class DetailsTagHandler: TagHandler() {

    override fun handle(visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag) {
        var summaryEnd = -1
        var summaryStart = -1
        for (child in tag.asBlock.children()) {

            if (!child.isClosed) {
                continue
            }

            if ("summary" == child.name()) {
                summaryStart = child.start()
                summaryEnd = child.end()
            }

            val tagHandler = renderer.tagHandler(child.name())
            if (tagHandler != null) {
                tagHandler.handle(visitor, renderer, child)
            } else if (child.isBlock) {
                visitChildren(visitor, renderer, child.asBlock)
            }
        }

        if (summaryEnd > -1 && summaryStart > -1) {
            val summary = visitor.builder().subSequence(summaryStart, summaryEnd)
            val summarySpan = DetailsSummarySpan(summary)
            visitor.builder().setSpan(summarySpan, summaryStart, summaryEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            visitor.builder().setSpan(DetailsParsingSpan(summarySpan), tag.start(), tag.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun supportedTags(): Collection<String> {
        return Collections.singleton("details")
    }
}

data class DetailsSummarySpan(val text: CharSequence)

enum class DetailsSpanState { DORMANT, CLOSED, OPENED }

data class DetailsParsingSpan(
    val summary: DetailsSummarySpan,
    var state: DetailsSpanState = DetailsSpanState.CLOSED
)