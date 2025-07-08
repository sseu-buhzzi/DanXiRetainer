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
data class OtReport(
	val reportId: Long? = null,
	val reason: String? = null,
	val result: String? = null,
	val content: String? = null,
	val floor: OtFloor? = null,
	val holeId: Long? = null,
	val timeCreated: String? = null,
	val timeUpdated: String? = null,
	val dealt: Boolean? = null,
	val dealtBy: Long? = null,
) {
	override fun hashCode() = checkNotNull(reportId).hashCode()

	override fun toString() =
		"OTReport{reportId: $reportId, reason: $reason, result:$result, content: $content, floor: $floor, holeId: $holeId, timeCreated: $timeCreated, timeUpdated: $timeUpdated, dealt: $dealt, dealtBy: $dealtBy}"

	override operator fun equals(other: Any?) =
		other is OtReport && reportId == other.reportId

	fun toJson() = dxrJson.encodeToJsonElement(this)

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtReport>(json)
	}
}
