package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Path
import java.time.OffsetDateTime
import kotlin.io.path.pathString
import kotlin.reflect.KFunction

sealed class DxrRetentionRequest(
	val path: Path,
	val retention: JsonElement,
) {
	class UserDemand(path: Path, retention: JsonElement) : DxrRetentionRequest(path, retention) {
		override val requestJson
			get() = buildJsonObject {
				put("type", TYPE)
				put("time", timeJsonObject)
				put("path", path.pathString)
				put("retention", retention)
			}

		companion object {
			const val TYPE = "user_demand"
		}
	}

	class AfterFetchRequest(
		path: Path,
		retention: JsonElement,
		val function: KFunction<*>,
	) : DxrRetentionRequest(path, retention) {
		override val requestJson
			get() = buildJsonObject {
				put("type", TYPE)
				put("time", timeJsonObject)
				put("path", path.pathString)
				put("retention", retention)
				put("function", function.name)
			}

		companion object {
			const val TYPE = "after_http_request"
		}
	}

	class TimerFire(
		path: Path,
		retention: JsonElement,
	) : DxrRetentionRequest(path, retention) {
		override val requestJson
			get() = buildJsonObject {
				put("type", TYPE)
				put("time", timeJsonObject)
				put("path", path.pathString)
			}

		companion object {
			const val TYPE = "timer_fire"
		}
	}

	abstract val requestJson: JsonElement

	val time: OffsetDateTime = OffsetDateTime.now()
	val timeJsonObject
		get() = buildJsonObject {
			put("year", time.year)
			put("month", time.monthValue)
			put("day", time.dayOfMonth)
			put("hour", time.hour)
			put("minute", time.minute)
			put("second", time.second)
			put("nano", time.nano)
		}
}
