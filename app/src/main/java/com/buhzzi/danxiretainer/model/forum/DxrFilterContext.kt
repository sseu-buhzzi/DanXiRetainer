package com.buhzzi.danxiretainer.model.forum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import java.nio.file.Path

abstract class DxrFilterContext(
	val path: Path,
	val filters: List<DxrFilter>,
) {
	val json
		get() = buildJsonObject {
			filters
				.filter { it.active }
				.forEach { put(it.key, it.json) }
		}

	fun store() {
		DxrRetention.storeFilterContextJson(path, json)
	}

	fun <T> predicate(item: T): Boolean {
		return filters.all { !it.active || it.predicate(item) }
	}
}

abstract class DxrFilter(
	val key: String,
) {
	abstract val json: JsonElement

	private var activeBack by mutableStateOf(false)
	open var active
		get() = activeBack
		protected set(value) {
			activeBack = value
		}

	@Composable
	abstract fun ToggleChipContent()

	@Composable
	abstract fun Content()

	abstract fun <T> predicate(item: T): Boolean

	open fun toggleActive() {
		active = !active
	}
}
