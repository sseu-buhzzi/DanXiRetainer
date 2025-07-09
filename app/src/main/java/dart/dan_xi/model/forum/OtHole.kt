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
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.dan_xi.provider.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class OtHole(
	val holeId: Long? = null,
	val divisionId: Long? = null,
	val timeCreated: String? = null,
	val timeUpdated: String? = null,
	val timeDeleted: String? = null,
	val tags: List<OtTag>? = null,
	val view: Long? = null,
	val reply: Long? = null,
	val floors: OtFloors? = null,
	val hidden: Boolean? = null,
	val locked: Boolean? = null,
) {
	val holeIdNotNull get() = checkNotNull(holeId) { this }
	val divisionIdNotNull get() = checkNotNull(divisionId) { this }
	val timeCreatedNotNull get() = checkNotNull(timeCreated) { this }
	val timeUpdatedNotNull get() = checkNotNull(timeUpdated) { this }
	val timeDeletedNotNull get() = checkNotNull(timeDeleted) { this }
	val tagsNotNull get() = checkNotNull(tags) { this }
	val viewNotNull get() = checkNotNull(view) { this }
	val replyNotNull get() = checkNotNull(reply) { this }
	val floorsNotNull get() = checkNotNull(floors) { this }
	val hiddenNotNull get() = checkNotNull(hidden) { this }
	val lockedNotNull get() = checkNotNull(locked) { this }

	fun toJson() = dxrJson.encodeToJsonElement(this)

	override fun equals(other: Any?) =
		other is OtHole && holeId == other.holeId

	override fun toString() =
		"OTHole{id: $holeId, divisionId: $divisionId, timeUpdated: $timeUpdated, timeCreated: $timeCreated, timeDeleted: $timeDeleted, tags: $tags, view: $view, reply: $reply, floors: $floors, hidden: $hidden, locked: $locked}"

	val isFolded get() = tags?.any { element ->
		element.name?.startsWith("*") == true
	}

	val isForceDeleted get() = runCatching {
		timeDeleted?.toDateTimeRfc3339()
	}.exceptionOrNull() == null

	override fun hashCode() = checkNotNull(holeId).hashCode()

	fun getSortingDateTime(sortOrder: SortOrder) = when (sortOrder) {
		SortOrder.LAST_REPLIED -> timeUpdated
		SortOrder.LAST_CREATED -> timeCreated
	}
		.let { checkNotNull(it) }
		.toDateTimeRfc3339()

	val floorsCount get() = checkNotNull(reply) + 1

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtHole>(json)

		/// Generate an empty BBSPost for special sakes.
		fun dummy() = OtHole(
			holeId = -1,
			divisionId = -1,
			timeCreated = "",
			timeUpdated = "",
			timeDeleted = "",
			tags = listOf(),
			view = -1,
			reply = -1,
			floors = null,
		)

		val dummyPost = dummy()
	}
}
