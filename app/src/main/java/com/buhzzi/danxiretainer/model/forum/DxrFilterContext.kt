package com.buhzzi.danxiretainer.model.forum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import java.nio.file.Path

interface DxrContentThrottler {
	val throttled: StateFlow<Boolean>

	fun resetThrottled()
}

abstract class DxrFilterContext(
	val path: Path,
	val filters: List<DxrFilter>,
) : DxrContentThrottler {
	val json
		get() = buildJsonObject {
			filters
				.filter { it.active }
				.forEach { put(it.key, it.json) }
		}

	fun store() {
		DxrRetention.storeFilterContextJson(path, json)
	}

	private var filteredOutNumber = 0
	private val throttledMutable = MutableStateFlow(false)
	override val throttled: StateFlow<Boolean> = throttledMutable

	fun <T> predicate(item: T): Boolean {
		filters.all { !it.active || it.predicate(item) } && return true
		if (++filteredOutNumber >= 16) {
			throttledMutable.value = true
		}
		return false
	}

	override fun resetThrottled() {
		filteredOutNumber = 0
		throttledMutable.value = false
	}
}

abstract class DxrFilter(
	val key: String,
) {
	abstract val json: JsonElement

	var active by mutableStateOf(false)
	var hidden by mutableStateOf(false)

	@Composable
	abstract fun ToggleChipContent()

	@Composable
	abstract fun Content()

	abstract fun <T> predicate(item: T): Boolean
}
