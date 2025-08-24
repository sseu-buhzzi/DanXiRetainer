package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import dart.package0.dan_xi.model.forum.OtTag
import dart.package0.flutter.src.material.Colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TagsSelector(
	tagLabels: Iterable<String>,
	shouldSuggestOnEmpty: Boolean = false,
	removeChip: (String) -> Unit,
	addChip: (String) -> Unit,
) {
	val snackbarProvider = LocalSnackbarProvider.current

	val scope = rememberCoroutineScope()
	var newLabel by remember { mutableStateOf("") }
	var boundsNullable by remember { mutableStateOf<Rect?>(null) }

	Column(
		modifier = Modifier
			.onGloballyPositioned { coordinates -> boundsNullable = coordinates.boundsInWindow() },
	) {
		FlowRow(
			modifier = Modifier
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
		) {
			tagLabels.forEach { tagLabel ->
				// TODO 从static toml中獲取highlighted tag names
				SelectTagChip(tagLabel, tagLabel == "highlighted") {
					scope.launch(Dispatchers.IO) {
						removeChip(tagLabel)
					}
				}
			}
		}
		TextField(
			newLabel,
			{ newLabel = it },
			modifier = Modifier
				.fillMaxWidth(),
			label = {
				Text(stringResource(R.string.filters_select_tags))
			},
		)
	}

	boundsNullable.takeIf { shouldSuggestOnEmpty || newLabel.trim().isNotEmpty() }?.let { bounds ->
		Popup(
			offset = IntOffset(0, bounds.height.toInt()),
		) {
			val density = LocalDensity.current
			val view = LocalView.current
			val systemInDarkTheme = isSystemInDarkTheme()

			val visibleRect = remember {
				android.graphics.Rect().also {
					view.getWindowVisibleDisplayFrame(it)
				}
			}

			val suggestedTags = remember { mutableStateListOf<OtTag>() }
			LaunchedEffect(newLabel) {
				var newLabelExisted = false
				val filteredTags = snackbarProvider.runShowing {
					DxrContent.loadTags(true)
						.filter {
							newLabelExisted = newLabelExisted || it.name == newLabel
							it.name?.lowercase()?.contains(newLabel) == true
						}
						.toList()
				}.getOrElse { emptyList() }
				suggestedTags.clear()
				if (!newLabelExisted) {
					suggestedTags.add(OtTag(null, 0, newLabel))
				}
				suggestedTags.addAll(filteredTags)
			}
			LazyColumn(
				modifier = Modifier
					.width(with(density) { bounds.width.toDp() })
					.heightIn(max = with(density) { (visibleRect.bottom - bounds.bottom).toDp() }),
			) {
				items(suggestedTags) { tag ->
					// `suggestedTags` have all tag name not null
					val name = tag.nameNotNull
					val color = tag.color(systemInDarkTheme)
					val temperature = tag.temperature ?: 0
					val icon = if (temperature > 0) {
						Icons.Default.Whatshot
					} else {
						Icons.Default.AddCircle
					}
					ListItem(
						{
							Text(name)
						},
						modifier = Modifier
							.clickable {
								scope.launch(Dispatchers.IO) {
									snackbarProvider.runShowing {
										addChip(name)
									}
								}
								newLabel = ""
							},
						colors = ListItemDefaults.colors(
							headlineColor = color,
							supportingColor = color,
							trailingIconColor = color,
						),
						supportingContent = {
							Text(buildString {
								append(temperature)
								tag.tagId?.let { append(" | tag#$it") }
							})
						},
						trailingContent = {
							Icon(icon, null)
						},
					)
				}
			}
		}
	}
}

@Composable
fun SelectTagChip(
	label: String,
	highlighted: Boolean = false,
	remove: () -> Unit = { },
) {
	val systemInDarkTheme = isSystemInDarkTheme()

	val shape = InputChipDefaults.shape
	val (color, contentColor) = if (highlighted) {
		Color.Transparent to Color.White
	} else {
		val effectiveColor = OtTag(name = label).color(systemInDarkTheme)
		effectiveColor to if (effectiveColor.luminance() <= 0.5) Color.White else Color.Black
	}
	Surface(
		modifier = Modifier
			.padding(4.dp)
			.run {
				if (highlighted) {
					background(Brush.horizontalGradient(listOf(Colors.blue, Colors.purple).map { materialColor ->
						materialColor.color.copy(0.875F)
					}), shape)
				} else {
					this
				}
			}
			.clickable(
				onClick = remove,
			),
		shape = shape,
		color = color,
		contentColor = contentColor,
	) {
		Row(
			modifier = Modifier
				.padding(4.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(label)
			Icon(
				Icons.Default.Cancel, null,
				modifier = Modifier
					.size(InputChipDefaults.IconSize),
			)
		}
	}
}
