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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.forum.DxrFilter
import com.buhzzi.danxiretainer.model.forum.DxrFilterContext
import com.buhzzi.danxiretainer.model.forum.DxrLocatedFloor
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.util.JavaScriptExecutor
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import com.buhzzi.danxiretainer.util.dxrJson
import dart.package0.dan_xi.model.forum.OtHole
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonArray
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
) {
	fun addTag(tagLabel: String) {
		// guaranteed not null
		val tagFilter = filters.find { filter -> filter.key == "tag" } as DxrTagFilter
		tagFilter.addTag(tagLabel)
	}
}

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
		val snackbarProvider = LocalSnackbarProvider.current

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState()),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
		) {
			val divisions by produceState(emptyList()) {
				snackbarProvider.runShowingWithContext(Dispatchers.IO) {
					value = DxrContent.loadDivisions()
				}
			}
			divisions.forEach { division ->
				val divisionId = division.divisionId?.toInt() ?: return@forEach
				FilterChip(
					divisionId in selections,
					{ selections.remove(divisionId) || selections.add(divisionId) },
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
		is OtHole -> item.tags
			?.mapNotNull { tag -> tag.name }
			?.containsAll(tagLabels)
			.let { it == true }

		else -> false
	}

	fun addTag(tagLabel: String) {
		active = true
		tagLabels.add(tagLabel)
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
			{
				regex = it.takeIf { it.isNotEmpty() }?.toRegex()
			},
			modifier = Modifier
				.fillMaxWidth(),
			label = {
				Text(stringResource(R.string.filters_content_regex))
			},
		)
	}

	override fun <T> predicate(item: T) = when (item) {
		is OtHole -> item.floors?.firstFloor?.content?.let { regex?.find(it) } != null
		is DxrLocatedFloor -> item.floor.content?.let { regex?.find(it) } != null
		else -> false
	}
}

private class DxrEvalFilter(initialJson: JsonObject) : DxrFilter("eval") {
	override val json get() = JsonPrimitive(filterJavaScript)

	private var filterJavaScript by (initialJson[key] as? JsonPrimitive)
		?.contentOrNull
		.let { mutableStateOf(it ?: "") }

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_eval))
	}

	@Composable
	override fun Content() {
		TextField(
			filterJavaScript,
			{ filterJavaScript = it },
			modifier = Modifier
				.fillMaxWidth(),
			label = {
				Text(stringResource(R.string.filters_eval_java_script))
			},
		)
	}

	override fun <T> predicate(item: T) = filterJavaScript == "" || when (item) {
		is OtHole -> {
			val holeJsonString = dxrJson.encodeToString<OtHole>(item)
			JavaScriptExecutor.execute("(hole => JSON.stringify(Boolean($filterJavaScript)))($holeJsonString);")
		}

		is DxrLocatedFloor -> {
			val (floor, hole, floorIndex) = item
			val floorJsonString = dxrJson.encodeToString(floor)
			val holeJsonString = dxrJson.encodeToString(hole)
			val floorIndexJsonString = dxrJson.encodeToString(floorIndex)
			JavaScriptExecutor.execute(
				"((floor, hole, index) => JSON.stringify(Boolean($filterJavaScript)))($floorJsonString, $holeJsonString, $floorIndexJsonString);",
			)
		}

		else -> JavaScriptExecutor.execute(filterJavaScript)
	} == "true"
}
