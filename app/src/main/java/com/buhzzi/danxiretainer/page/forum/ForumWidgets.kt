package com.buhzzi.danxiretainer.page.forum

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.retension.RetentionPageContent
import com.buhzzi.danxiretainer.page.retension.RetentionPageTopBar
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.page.settings.SettingsPageContent
import com.buhzzi.danxiretainer.page.settings.SettingsPageTopBar
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarController
import com.buhzzi.danxiretainer.util.MarkwonProvider
import dart.package0.dan_xi.model.forum.OtTag
import dart.package0.dan_xi.util.hashColor
import dart.package0.dan_xi.util.withLightness
import dart.package0.flutter.src.material.Colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ForumPages(
	val icon: @Composable () -> Unit,
	val label: @Composable () -> Unit,
	val topBar: @Composable () -> Unit,
	val content: @Composable (Modifier) -> Unit,
) {
	FORUM(
		{
			Icon(Icons.Default.Forum, null)
		},
		{
			Text(stringResource(R.string.forum_label))
		},
		{
			ForumPageTopBar()
		},
		{ modifier ->
			ForumPageContent(
				modifier = modifier,
			)
		},
	),
	RETENTION(
		{
			Icon(Icons.Default.Storage, null)
		},
		{
			Text(stringResource(R.string.retention_label))
		},
		{
			RetentionPageTopBar()
		},
		{ modifier ->
			RetentionPageContent(
				modifier = modifier,
			)
		},
	),
	SETTINGS(
		{
			Icon(Icons.Default.Settings, null)
		},
		{
			Text(stringResource(R.string.settings_label))
		},
		{
			SettingsPageTopBar()
		},
		{ modifier ->
			SettingsPageContent(
				modifier = modifier,
			)
		},
	),
}

@Composable
fun TagChipsRow(tags: List<OtTag>) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth(),
	) {
		tags.forEach { tag ->
			// TODO 从static toml中獲取highlighted tag names
			TagChip(tag, tag.name == "highlighted")
		}
	}
}

@Composable
fun TagChip(
	tag: OtTag,
	highlighted: Boolean = false,
) {
	val snackbarController = LocalSnackbarController.current
	val holesFilterContext = LocalFilterContext.current as? DxrHolesFilterContext

	val systemInDarkTheme = isSystemInDarkTheme()

	val scope = rememberCoroutineScope()

	val shape = ShapeDefaults.ExtraSmall
	val modifier = Modifier
		.padding(2.dp)
		.run {
			if (highlighted) {
				background(Brush.horizontalGradient(listOf(Colors.blue, Colors.purple).map { materialColor ->
					materialColor.color.copy(0.875F)
				}), shape)
			} else {
				this
			}
		}
		.clickable {
			scope.launch(Dispatchers.IO) {
				runCatchingOnSnackbar(snackbarController) {
					val filterContext = holesFilterContext
						?: DxrRetention.loadHolesFilterContext(DxrSettings.Models.userProfileNotNull.userIdNotNull)
					filterContext.addTag(tag.nameNotNull)
					holesFilterContext ?: filterContext.store()
				}
			}
		}
	val (color, contentColor) = if (highlighted) {
		Color.Transparent to Color.White
	} else {
		val effectiveColor = tag.color(systemInDarkTheme)
		effectiveColor.copy(0.5F) to effectiveColor.withLightness { (_, _, lightness) ->
			if (systemInDarkTheme) {
				lightness + 0.125F
			} else {
				lightness - 0.125F
			}.coerceIn(0F, 1F)
		}
	}
	Surface(
		modifier = modifier,
		shape = shape,
		color = color,
		contentColor = contentColor,
	) {
		Text(
			tag.nameNotNull,
			modifier = Modifier
				.padding(4.dp, 0.dp),
		)
	}
}

@Composable
fun AnonynameRow(anonyname: String, posterOriginal: Boolean) {
	Row(
		modifier = Modifier
			.height(IntrinsicSize.Min)
			.padding(4.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		val systemInDarkTheme = isSystemInDarkTheme()
		val anonynameColor = anonyname.hashColor(systemInDarkTheme) ?: Color.Red
		VerticalDivider(
			modifier = Modifier
				.padding(4.dp),
			thickness = 4.dp,
			color = anonynameColor,
		)
		if (posterOriginal) {
			Surface(
				modifier = Modifier
					.padding(2.dp),
				shape = ShapeDefaults.ExtraSmall,
				color = anonynameColor,
				// using `<= 0.5` instead of `< 0.5` to stay consistent with DanXi. Make them happy
				contentColor = if (anonynameColor.luminance() <= 0.5) Color.White else Color.Black,
			) {
				Text(
					// TODO 可選項LZ・DZ・或OP
					"LZ",
					modifier = Modifier
						.padding(4.dp, 0.dp),
					fontSize = 14.sp,
					fontWeight = FontWeight.Bold,
					lineHeight = 16.sp,
				)
			}
		}
		Text(
			anonyname,
			color = anonynameColor,
			fontWeight = FontWeight.Bold,
		)
	}
}

@Composable
fun FloorContentRenderer(content: String) {
	AndroidView(
		{ context ->
			TextView(context).also { view ->
				MarkwonProvider(context).setMarkdown(view, content)
			}
		},
	)
}
