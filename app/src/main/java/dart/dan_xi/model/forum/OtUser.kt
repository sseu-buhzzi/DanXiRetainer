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
data class OtUser(
	val userId: Long? = null,
	val nickname: String? = null,
	val favorites: List<Long>? = null,
	val subscriptions: List<Long>? = null,
	val permission: OtUserPermission? = null,
	val config: OtUserConfig? = null,
	val joinedTime: String? = null,
	val isAdmin: Boolean? = null,
	val hasAnsweredQuestions: Boolean? = null,
) {
	val userIdNotNull get() = checkNotNull(userId) { this }
	val nicknameNotNull get() = checkNotNull(nickname) { this }
	val favoritesNotNull get() = checkNotNull(favorites) { this }
	val subscriptionsNotNull get() = checkNotNull(subscriptions) { this }
	val permissionNotNull get() = checkNotNull(permission) { this }
	val configNotNull get() = checkNotNull(config) { this }
	val joinedTimeNotNull get() = checkNotNull(joinedTime) { this }
	val isAdminNotNull get() = checkNotNull(isAdmin) { this }
	val hasAnsweredQuestionsNotNull get() = checkNotNull(hasAnsweredQuestions) { this }

	fun toJson() = dxrJson.encodeToJsonElement(this)

	override fun equals(other: Any?) = other is OtUser && userId == other.userId

	override fun hashCode() = checkNotNull(userId).hashCode()

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtUser>(json)
	}
}
