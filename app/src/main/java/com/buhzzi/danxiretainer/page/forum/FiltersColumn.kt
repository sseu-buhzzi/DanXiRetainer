package com.buhzzi.danxiretainer.page.forum

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarController
import com.buhzzi.danxiretainer.util.dxrJson
import dart.package0.dan_xi.model.forum.OtDivision
import dart.package0.dan_xi.model.forum.OtHole
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Composable
fun FiltersColumn() {
	val filterContext = LocalFilterContext.current

	println("filterJson: ${dxrJson.encodeToString(filterContext.json)}")

	var allFiltersShown by remember { mutableStateOf(true) }

	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(
				{ allFiltersShown = !allFiltersShown },
			) {
				if (allFiltersShown) {
					Icon(Icons.Default.FilterAlt, null)
				} else {
					Icon(Icons.Default.FilterAltOff, null)
				}
			}
			LazyRow(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				items(filterContext.filters) { filter ->
					FilterChip(
						filter.active,
						{ filter.toggleActive() },
						{
							filter.ToggleChipContent()
						}
					)
				}
			}
		}
		filterContext.filters.forEach { filter ->
			AnimatedVisibility(allFiltersShown && filter.active) {
				filter.Content()
			}
		}
	}
}

class DxrDivisionFilter(initialJson: JsonObject) : DxrFilter("division") {
	override val json
		get() = buildJsonArray {
			selections.sorted().forEach { add(it) }
		}

	private val selections = mutableStateSetOf<Int>().apply {
		(initialJson[key] as? JsonArray)
			?.mapNotNull { (it as? JsonPrimitive)?.intOrNull }
			?.let { addAll(it) }
			?.let { active = true }
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

	override fun <T> predicate(item: T): Boolean {
		// TODO disable it when using ad hoc logic in forum API
		val hole = item as? OtHole ?: return false
		return hole.divisionId?.toInt() in selections
	}
}

class DxrTagFilter(initialJson: JsonObject) : DxrFilter("tag") {
	override val json
		get() = buildJsonArray {
			tagLabels.sorted().forEach { add(it) }
		}

	private val tagLabels = mutableStateSetOf<String>().apply {
		(initialJson[key] as? JsonArray)
			?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
			?.let { addAll(it) }
			?.let { active = true }
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

	override fun <T> predicate(item: T): Boolean {
		val hole = item as? OtHole ?: return false
		return hole.tags?.find { tag -> tag.name in tagLabels } != null
	}
}

class DxrEvalFilter(initialJson: JsonObject) : DxrFilter("eval") {
	override val json get() = JsonNull

	init {
		active = true
	}

	@Composable
	override fun ToggleChipContent() {
		Text(stringResource(R.string.filters_eval))
	}

	@Composable
	override fun Content() {
		// TODO("Not yet implemented")
	}

	override fun <T> predicate(item: T): Boolean {
		// TODO("Not yet implemented")
		return true
	}

	override fun toggleActive() {}
}
