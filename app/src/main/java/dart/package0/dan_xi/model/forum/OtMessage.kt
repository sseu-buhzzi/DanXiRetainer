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

package dart.package0.dan_xi.model.forum

import com.buhzzi.danxiretainer.util.dxrJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class OtMessage(
	val messageId: Long? = null,
	val message: String? = null,
	val code: String? = null,
	val timeCreated: String? = null,
	val hasRead: Boolean? = null,

	/// This can be anything, in json format
	val data: JsonObject? = null,
	val description: String? = null,
) {
	val messageIdNotNull get() = checkNotNull(messageId) { this }
	val messageNotNull get() = checkNotNull(message) { this }
	val codeNotNull get() = checkNotNull(code) { this }
	val timeCreatedNotNull get() = checkNotNull(timeCreated) { this }
	val hasReadNotNull get() = checkNotNull(hasRead) { this }
	val dataNotNull get() = checkNotNull(data) { this }
	val descriptionNotNull get() = checkNotNull(description) { this }

	fun toJson() = dxrJson.encodeToJsonElement(this)

	override fun equals(other: Any?) = other is OtMessage && messageId == other.messageId

	override fun hashCode() = checkNotNull(messageId).hashCode()

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtMessage>(json)
	}
}
