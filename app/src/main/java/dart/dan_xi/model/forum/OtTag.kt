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

import androidx.compose.ui.graphics.Color
import com.buhzzi.danxiretainer.util.dxrJson
import dart.dan_xi.util.hashColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class OtTag(
	val tagId: Long? = null,
	val temperature: Long? = null,
	val name: String? = null,
) {
	val tagIdNotNull get() = checkNotNull(tagId) { this }
	val temperatureNotNull get() = checkNotNull(temperature) { this }
	val nameNotNull get() = checkNotNull(name) { this }

	fun toJson() = dxrJson.encodeToJsonElement(this)

	override operator fun equals(other: Any?) =
		other is OtTag && tagId == other.tagId

	override fun hashCode() = tagIdNotNull.hashCode()

	fun getColor(systemInDarkTheme: Boolean) =
		name?.hashColor(systemInDarkTheme) ?: Color.Red

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtTag>(json)
	}
}
