package com.buhzzi.danxiretainer.page.forum

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.forum.DxrFilter
import com.buhzzi.danxiretainer.model.forum.DxrFilterContext
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarController
import dart.package0.dan_xi.model.forum.OtDivision
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import java.nio.file.Path

@Composable
fun FiltersColumn() {
	val filterContext = LocalFilterContext.current

	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			LazyRow(
				modifier = Modifier
					.weight(1F)
					.padding(horizontal = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				items(filterContext.filters) { filter ->
					FilterChip(
						filter.active,
						{ filter.active = !filter.active },
						{
							filter.ToggleChipContent()
						},
					)
					AnimatedVisibility(filter.active) {
						FilterChip(
							!filter.hidden,
							{ filter.hidden = !filter.hidden },
							if (filter.hidden) {
								{
									Icon(Icons.Default.VisibilityOff, null)
								}
							} else {
								{
									Icon(Icons.Default.Visibility, null)
								}
							},
						)
					}
				}
			}
			val allHidden = filterContext.filters.all { filter -> !filter.active || filter.hidden }
			IconButton(
				{
					filterContext.filters.forEach { filter ->
						filter.hidden = filter.active && !allHidden
					}
				},
				content = if (allHidden) {
					{
						Icon(Icons.Default.FilterAltOff, null)
					}
				} else {
					{
						Icon(Icons.Default.FilterAlt, null)
					}
				},
			)
		}
		filterContext.filters.forEach { filter ->
			AnimatedVisibility(filter.active && !filter.hidden) {
				filter.Content()
			}
		}
	}
}

class DxrHolesFilterContext(path: Path) : DxrFilterContext(
	path,
	DxrRetention.loadFilterContextJson(path).let { initialJson ->
		listOf(
			DxrDivisionFilter(initialJson),
			DxrTagFilter(initialJson),
			DxrContentFilter(initialJson),
		)
	},
)

class DxrFloorsFilterContext(path: Path) : DxrFilterContext(
	path,
	DxrRetention.loadFilterContextJson(path).let { initialJson ->
		listOf(
			DxrContentFilter(initialJson),
			DxrEvalFilter(initialJson),
		)
	}
)

private class DxrDivisionFilter(initialJson: JsonObject) : DxrFilter("division") {
	override val json
		get() = buildJsonArray {
			selections.sorted().forEach { add(it) }
		}

	private val selections = (initialJson[key] as? JsonArray)
		?.mapNotNull { (it as? JsonPrimitive)?.intOrNull }
		?.toTypedArray()
		.let { mutableStateSetOf(*it ?: emptyArray()) }

	init {
		active = selections.isNotEmpty()
	}

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_division))
	}

	@Composable
	override fun Content() {
		val snackbarController = LocalSnackbarController.current

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState()),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
		) {
			val divisions = remember { mutableStateListOf<OtDivision>() }
			LaunchedEffect(Unit) {
				runCatchingOnSnackbar(snackbarController) {
					val loadedDivisions = DxrContent.loadDivisions()
					divisions.clear()
					divisions.addAll(loadedDivisions)
				}
			}
			divisions.forEach { division ->
				val divisionId = division.divisionId?.toInt() ?: return@forEach
				FilterChip(
					divisionId in selections,
					{
						selections.remove(divisionId) || selections.add(divisionId)
					},
					{
						Text(division.name ?: "?")
					},
					leadingIcon = if (divisionId in selections) {
						{
							Icon(Icons.Default.Done, null)
						}
					} else {
						null
					},
				)
			}
		}
	}

	override fun <T> predicate(item: T) = when (item) {
		is OtHole -> item.divisionId?.toInt() in selections
		else -> false
	}
}

private class DxrTagFilter(initialJson: JsonObject) : DxrFilter("tag") {
	override val json
		get() = buildJsonArray {
			tagLabels.sorted().forEach { add(it) }
		}

	private val tagLabels = (initialJson[key] as? JsonArray)
		?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
		?.toTypedArray()
		.let { mutableStateSetOf(*it ?: emptyArray()) }

	init {
		active = tagLabels.isNotEmpty()
	}

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_tag))
	}

	@Composable
	override fun Content() {
		TagsSelector(
			tagLabels,
			false,
			{ tagLabel -> tagLabels.remove(tagLabel) },
		) { tagLabel -> tagLabels.add(tagLabel) }
	}

	override fun <T> predicate(item: T) = when (item) {
		is OtHole -> item.tags?.find { tag -> tag.name in tagLabels } != null
		else -> false
	}
}

private class DxrContentFilter(initialJson: JsonObject) : DxrFilter("content") {
	override val json
		get() = JsonPrimitive(regex?.pattern)

	private var regex by (initialJson[key] as? JsonPrimitive)
		?.contentOrNull
		?.toRegex()
		.let { mutableStateOf(it) }

	init {
		active = regex != null
	}

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_content))
	}

	@Composable
	override fun Content() {
		TextField(
			regex?.pattern ?: "",
			{ regex = it.takeIf { it.isNotEmpty() }?.toRegex() },
			modifier = Modifier
				.fillMaxWidth(),
			label = {
				Text(stringResource(R.string.filters_content_regex))
			},
		)
	}

	override fun <T> predicate(item: T) = when (item) {
		is OtHole -> item.floors?.firstFloor?.content?.let { regex?.find(it) } != null
		is OtFloor -> item.content?.let { regex?.find(it) } != null
		else -> false
	}
}

private class DxrEvalFilter(initialJson: JsonObject) : DxrFilter("eval") {
	override val json get() = JsonNull

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_eval))
	}

	@Composable
	override fun Content() {
		// TODO("Not yet implemented")
	}

	override fun <T> predicate(item: T) =
		// TODO("Not yet implemented")
		true
}
