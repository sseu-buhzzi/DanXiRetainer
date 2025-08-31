package com.buhzzi.danxiretainer.page.forum

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.withSave
import androidx.core.net.toUri
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.httpResourcePathOf
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.formatter.Formatter
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler
import com.vladsch.flexmark.util.sequence.BasedSequence
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.RenderProps
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.AsyncDrawableSpan
import io.noties.markwon.image.ImageProps
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolver
import io.noties.markwon.image.ImageSpanFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.path.pathString

@Composable
fun MarkdownContentRenderer(content: String) {
	val context = LocalContext.current
	var recomposingCounter by remember { mutableIntStateOf(0) }
	val parsedContent by produceState(content) {
		val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
		val parser = Parser.builder().build()
		val document = parser.parse(content)
		val visitor = NodeVisitor(
			VisitHandler(Image::class.java) { image ->
				val originalUrl = image.url.toString().toUri()
				val cachedPath = context.httpResourcePathOf(userId, originalUrl)
				val replacedSequence = BasedSequence.of(cachedPath?.pathString ?: "")
				// Modifying URL (with `image.url = ...`) does not work to change the rebuilt Markdown text
				// Debugging with flexmark, it is found that the appended text is not `image.url` but `image.pageRef`.
				// Some helpful breakpoints (to find out what are written into the rebuilt content) in library
				// `com.vladsch.flexmark:flexmark-util-sequence:0.64.8`, file
				// `com/vladsch/flexmark/util/sequence/LineAppendableImpl.java` are at line 360, 547, 564, 573, 599, 623, 881.
				// The standard method to modify the URL is `image.setUrlChars(...)`.
				image.setUrlChars(replacedSequence)

				cachedPath ?: return@VisitHandler
				launch(Dispatchers.IO) {
					DxrRetention.cacheHttpResource(originalUrl, cachedPath)
					++recomposingCounter
					Log.d("MarkdownContentRenderer", "cache http resource $originalUrl $cachedPath")
				}
			},
		)
		visitor.visit(document)
		val formatter = Formatter.builder().build()
		value = formatter.render(document)
	}
	key(recomposingCounter) {
		// optional TODO make height configurable in settings
		val imageSpanFactory = remember { FixedHeightImageSpanFactory(512) }
		MarkdownText(
			parsedContent,
			modifier = Modifier
				.fillMaxWidth(),
			imageRequestBuilder = {
				placeholder(R.drawable.markdown_image_loading)
				error(R.drawable.markdown_image_failed_to_load)
			},
			imageSpanFactory = imageSpanFactory,
		)
	}
}

private class FixedHeightImageSpanFactory(val canvasHeight: Int) : ImageSpanFactory() {
	override fun getSpans(configuration: MarkwonConfiguration, props: RenderProps) = AsyncDrawableSpan(
		configuration.theme(),
		FixedHeightAsyncDrawable(
			canvasHeight,
			ImageProps.DESTINATION.require(props),
			configuration.asyncDrawableLoader(),
			configuration.imageSizeResolver(),
			ImageProps.IMAGE_SIZE.get(props),
		),
		AsyncDrawableSpan.ALIGN_BOTTOM,
		ImageProps.REPLACEMENT_TEXT_IS_LINK.get(props, false),
	)
}

private class FixedHeightAsyncDrawable(
	val canvasHeight: Int,
	destination: String,
	loader: AsyncDrawableLoader,
	imageSizeResolver: ImageSizeResolver,
	imageSize: ImageSize?,
) : AsyncDrawable(
	destination,
	loader,
	imageSizeResolver,
	imageSize,
) {
	// not a fix, it is only a more easily understandable implementation
	private val canvasWidth
		get() = lastKnownCanvasWidth.takeIf { it > 0 }
			?.also { publicLastKnownCanvasWidth = it }
			?: publicLastKnownCanvasWidth

	private var innerBounds: Rect? = null

	override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
		super.setBounds(
			left,
			top,
			maxOf(right, left + canvasWidth),
			bottom,
		)
	}

	@Synchronized
	override fun setResult(result: Drawable) {
		innerBounds = null
		constrainResultInFixedHeight(result, canvasWidth)
		super.setResult(result)
	}

	@Synchronized
	override fun clearResult() {
		innerBounds = null
		super.clearResult()
	}

	@Synchronized
	override fun initWithKnownDimensions(width: Int, textSize: Float) {
		constrainResultInFixedHeight(result, width)
		super.initWithKnownDimensions(width, textSize)
	}

	override fun draw(canvas: Canvas) {
		result ?: return
		val canvasWidth = canvasWidth
		val innerBounds = innerBounds?.takeIf {
			canvasWidth > 0 && !it.isEmpty
		} ?: run {
			result.draw(canvas)
			return
		}
		val width = innerBounds.width()
		val height = innerBounds.height()
		canvas.withSave {
			val sx = width * canvasHeight / (height * canvasWidth).toFloat()
			if (sx <= 1) {
				val dx = canvasWidth * (1 - sx) / 2
				translate(dx, 0F)
				scale(sx, 1F)
			} else {
				val sy = 1 / sx
				val dy = canvasHeight * (1 - sy) / 2
				translate(0F, dy)
				scale(1F, sy)
			}
			result.draw(this)
		}
	}

	@Synchronized
	private fun constrainResultInFixedHeight(result: Drawable?, canvasWidth: Int) {
		result ?: return
		canvasWidth > 0 || return
		innerBounds?.let { return }
		innerBounds = Rect(result.bounds)
		result.setBounds(0, 0, canvasWidth, canvasHeight)
	}

	companion object {
		private var publicLastKnownCanvasWidth = 1
	}
}
