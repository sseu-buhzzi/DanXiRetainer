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
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

// Represents a question in the quiz popped out after register
@Serializable
data class QuizAnswer(
	val answer: List<String>? = null,
	val id: Long? = null,
) {
	fun toJson() = dxrJson.encodeToJsonElement(this)

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<QuizAnswer>(json)
	}
}
