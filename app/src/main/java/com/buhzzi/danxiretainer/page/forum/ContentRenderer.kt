package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.formatter.Formatter
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownContentRenderer(content: String) {
	val parsedContent = remember(content) {
		val parser = Parser.builder().build()
		val document = parser.parse(content)
		val visitor = NodeVisitor(
			VisitHandler(Image::class.java) { image ->
				println("visit image: $image")
			}
		)
		visitor.visit(document)
		// TODO prefetch images and rebuild content
		val formatter = Formatter.builder().build()
		formatter.render(document)
	}
	MarkdownText(
		parsedContent,
		modifier = Modifier
			.fillMaxWidth(),
	)
}
