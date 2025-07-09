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

import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.cleanMode
import com.buhzzi.danxiretainer.util.dxrJson
import dart.dan_xi.util.forum.CleanModeFilter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class OtFloor(
	val floorId: Long? = null,
	val holeId: Long? = null,
	val content: String? = null,
	val anonyname: String? = null,
	val timeCreated: String? = null,
	val timeUpdated: String? = null,
	val deleted: Boolean? = null,
	val fold: List<String>? = null,
	val like: Long? = null,
	val isMe: Boolean? = null,
	val liked: Boolean? = null,
	val mention: List<OtFloor>? = null,
	val dislike: Long? = null,
	val disliked: Boolean? = null,
	val specialTag: String? = null,
	val modified: Long? = null,
) {
	val floorIdNotNull get() = checkNotNull(floorId) { this }
	val holeIdNotNull get() = checkNotNull(holeId) { this }
	val contentNotNull get() = checkNotNull(content) { this }
	val anonynameNotNull get() = checkNotNull(anonyname) { this }
	val timeCreatedNotNull get() = checkNotNull(timeCreated) { this }
	val timeUpdatedNotNull get() = checkNotNull(timeUpdated) { this }
	val deletedNotNull get() = checkNotNull(deleted) { this }
	val foldNotNull get() = checkNotNull(fold) { this }
	val likeNotNull get() = checkNotNull(like) { this }
	val isMeNotNull get() = checkNotNull(isMe) { this }
	val likedNotNull get() = checkNotNull(liked) { this }
	val mentionNotNull get() = checkNotNull(mention) { this }
	val dislikeNotNull get() = checkNotNull(dislike) { this }
	val dislikedNotNull get() = checkNotNull(disliked) { this }
	val specialTagNotNull get() = checkNotNull(specialTag) { this }
	val modifiedNotNull get() = checkNotNull(modified) { this }

	fun toJson() = dxrJson.encodeToJsonElement(this)

	/// Check whether the object has a valid position (i.it. valid floor and hole id).
	val valid get() =
		(floorId ?: -1) > 0 && (holeId ?: -1) > 0

	override operator fun equals(other: Any?) =
		other is OtFloor && floorId == other.floorId

	fun copyWith(
		floorId: Long? = null,
		holeId: Long? = null,
		content: String? = null,
		anonyname: String? = null,
		timeCreated: String? = null,
		timeUpdated: String? = null,
		deleted: Boolean? = null,
		fold: List<String>? = null,
		like: Long? = null,
		isMe: Boolean? = null,
		liked: Boolean? = null,
		mention: List<OtFloor>? = null,
		dislike: Long? = null,
		disliked: Boolean? = null,
	) = copy(
		floorId = floorId,
		holeId = holeId,
		content = content,
		anonyname = anonyname,
		timeCreated = timeCreated,
		timeUpdated = timeUpdated,
		deleted = deleted,
		fold = fold,
		like = like,
		isMe = isMe,
		liked = liked,
		mention = mention,
		dislike = dislike,
		disliked = disliked,
	)

	val filteredContent get() = content?.let { _ ->
		if (DxrSettings.Items.cleanMode == true) {
			CleanModeFilter.cleanText(content)
		} else {
			content
		}
	}
	val filteredContentNotNull get() = checkNotNull(filteredContent) { this }

	val deleteReason get() = content.takeIf { deleted == true }

	val foldReason get() = fold?.takeIf { it.isNotEmpty() }?.joinToString(" ")

	override fun toString() = "OTFloor{floorId: $floorId, holeId: $holeId, content: $content, anonyname: $anonyname, timeUpdated: $timeUpdated, timeCreated: $timeCreated, specialTag: $specialTag, deleted: $deleted, isMe: $isMe, liked: $liked, fold: $fold, modified: $modified, like: $like, mention: $mention}"

	override fun hashCode() = floorId?.hashCode() ?: 0

	companion object {
		fun fromJson(json: JsonElement) = dxrJson.decodeFromJsonElement<OtFloor>(json)

		/// Generate an empty BBSPost for special sakes.
		fun dummy() = OtFloor(
			floorId = -1,
			holeId = -1,
			content = "",
			anonyname = "",
			timeCreated = "",
			timeUpdated = "",
			deleted = false,
			fold = emptyList(),
			like = 0,
			isMe = false,
			liked = false,
			mention = emptyList(),
			dislike = 0,
			disliked = false,
		)

		fun special(
			title: String,
			content: String,
			holeId: Long? = null,
			floorId: Long? = null,
		) = OtFloor(
			floorId = floorId ?: 0,
			holeId = holeId ?: 0,
			content = content,
			anonyname = title,
			timeCreated = "",
			timeUpdated = "",
			deleted = false,
			fold = emptyList(),
			like = 0,
			isMe = false,
			liked = false,
			mention = emptyList(),
			dislike = 0,
			disliked = false,
		)

		fun onlyId(floorId: Long) = special("", "", null, floorId)
	}
}
