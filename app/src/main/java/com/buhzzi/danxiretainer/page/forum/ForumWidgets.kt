package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.retension.RetentionPageContent
import com.buhzzi.danxiretainer.page.retension.RetentionPageTopBar
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.page.settings.SettingsPageContent
import com.buhzzi.danxiretainer.page.settings.SettingsPageTopBar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarController
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
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
fun AnonynameRow(
	floor: OtFloor,
	hole: OtHole,
	floorIndex: Int,
	modifier: Modifier = Modifier,
) {
	Layout(
		modifier = modifier
			.padding(4.dp),
		content = {
			val systemInDarkTheme = isSystemInDarkTheme()
			val anonyname = floor.anonyname ?: "?"
			val anonynameColor = anonyname.hashColor(systemInDarkTheme) ?: Color.Red
			VerticalDivider(
				modifier = Modifier
					.padding(4.dp),
				thickness = 4.dp,
				color = anonynameColor,
			)
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				itemVerticalAlignment = Alignment.CenterVertically,
			) {
				if (floor.anonyname?.equals(hole.floors?.firstFloor?.anonyname) == true) {
					// TODO 可選項LZ・DZ・或OP
					AnonynameDecorationChip("LZ", anonynameColor)
				}
				Text(
					anonyname,
					color = anonynameColor,
					fontWeight = FontWeight.Bold,
				)
				if (floor.deleted == true) {
					AnonynameDecorationChip(stringResource(R.string.floor_deleted), MaterialTheme.colorScheme.primary)
				}
				if (floor.specialTag?.isNotEmpty() == true) {
					AnonynameDecorationChip(floor.specialTag, Color.Red)
				}
				// We will only show the hidden tag if this hole is hidden
				// and this floor is the first floor.
				if (floorIndex == 0 && hole.hidden == true) {
					AnonynameDecorationChip(stringResource(R.string.hole_hidden), Color.Red)
				}
				// Ditto.
				if (floorIndex == 0 && hole.isForceDeleted) {
					AnonynameDecorationChip(stringResource(R.string.hole_deleted), Color.Red)
				}
				// Show locked tag if the hole is locked and this is the first floor
				if (floorIndex == 0 && hole.locked == true) {
					AnonynameDecorationChip(stringResource(R.string.hole_locked), MaterialTheme.colorScheme.primary)
				}
				var isPinned by remember { mutableStateOf(false) }
				LaunchedEffect(Unit) {
					isPinned = DxrContent.loadDivisions().any { division ->
						division.pinned?.any { it == hole } == true
					}
				}
				// Show pinned tag if this hole is in the pinned list and this is the first floor
				if (floorIndex == 0 && isPinned) {
					AnonynameDecorationChip(stringResource(R.string.hole_pinned), MaterialTheme.colorScheme.primary)
				}
			}
		},
	) { measurables, constraints ->
		println("measurables: ${measurables.map { "$it" }}")
		println("constraints: $constraints")
		val width = constraints.minWidth
		val dividerWidth = measurables[0].minIntrinsicWidth(0)
		val rowWidth = width - dividerWidth
		// `Measurable` using `minIntrinsicHeight` or `maxIntrinsicHeight` won't take the last row in `FlowRow` in count
		val rowPlaceable = measurables[1].measure(
			Constraints(
				minWidth = rowWidth,
				maxWidth = rowWidth,
			)
		)
		val height = rowPlaceable.height
		val dividerPlaceable = measurables[0].measure(
			Constraints(
				minHeight = height,
				maxHeight = height,
			)
		)
		layout(width, height) {
			dividerPlaceable.placeRelative(0, 0)
			rowPlaceable.placeRelative(dividerWidth, 0)
		}
	}
}

@Composable
private fun AnonynameDecorationChip(label: String, color: Color) {
	Surface(
		modifier = Modifier
			.padding(2.dp),
		shape = ShapeDefaults.ExtraSmall,
		color = color,
		// using `<= 0.5` instead of `< 0.5` to stay consistent with DanXi. Make them happy
		contentColor = if (color.luminance() <= 0.5) Color.White else Color.Black,
	) {
		Text(
			label,
			modifier = Modifier
				.padding(4.dp, 0.dp),
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			lineHeight = 16.sp,
		)
	}
}
