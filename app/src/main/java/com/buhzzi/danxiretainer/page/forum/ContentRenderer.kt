package com.buhzzi.danxiretainer.page.forum

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.path.pathString

@Composable
fun MarkdownContentRenderer(content: String) {
	val context = LocalContext.current
	var parsedContent by remember { mutableStateOf(content) }
	var recomposingCounter by remember { mutableIntStateOf(0) }
	LaunchedEffect(content) {
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
			}
		)
		visitor.visit(document)
		val formatter = Formatter.builder().build()
		parsedContent = formatter.render(document)
	}
	key(recomposingCounter) {
		MarkdownText(
			parsedContent,
			modifier = Modifier
				.fillMaxWidth(),
			// optional TODO make height configurable in settings
			imageHeight = 512,
		)
	}
}
