/*
 *     Portions translated from original Dart source of DanXi-Dev
 *
 *     Copyright (C) 2021  DanXi-Dev
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dart.dan_xi.model.forum

import com.buhzzi.danxiretainer.util.dxrJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class OtAudit(
	val content: String,
	val holeId: Long,
	val id: Long,
	val isActualSensitive: Boolean? = null,
	val modified: Long,
	val timeCreated: String? = null,
	val timeUpdated: String? = null,
	val sensitiveDetail: String? = null,
) {
	val isActualSensitiveNotNull get() = checkNotNull(isActualSensitive) { this }
	val modifiedNotNull get() = checkNotNull(modified) { this }
	val timeCreatedNotNull get() = checkNotNull(timeCreated) { this }
	val timeUpdatedNotNull get() = checkNotNull(timeUpdated) { this }
	val sensitiveDetailNotNull get() = checkNotNull(sensitiveDetail) { this }

	val processed get() = OtAudit(
		content = "已處理",
		holeId = holeId,
		id = id,
		isActualSensitive = isActualSensitive,
		modified = modified,
		timeCreated = timeCreated,
		timeUpdated = timeUpdated,
		sensitiveDetail = sensitiveDetail,
	)

	fun toJson() = dxrJson.encodeToJsonElement(this)

	override fun toString() =
		"OTAudit{content: $content, holeId: $holeId, id: $id, isActualSensitive: $isActualSensitive, modified: $modified, timeCreated: $timeCreated, timeUpdated: $timeUpdated, sensitiveDetail: $sensitiveDetail}"

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtAudit>(json)
	}
}
