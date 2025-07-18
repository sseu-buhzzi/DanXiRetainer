package com.buhzzi.danxiretainer.repository.content

import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.sortOrderOrDefault
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime

object DxrContent {
	suspend fun loadHole(holeId: Long) = when (DxrSettings.Models.contentSourceOrDefault) {
		DxrContentSource.FORUM_API -> DxrForumApi.loadHoleById(holeId)
		DxrContentSource.RETENTION -> {
			val userProfile = DxrSettings.Models.userProfileNotNull
			val userId = userProfile.userIdNotNull

			val hole = DxrRetention.loadHole(userId, holeId)
			checkNotNull(hole)
		}
	}

	suspend fun loadFloor(floorId: Long) = when (DxrSettings.Models.contentSourceOrDefault) {
		DxrContentSource.FORUM_API -> DxrForumApi.loadFloorById(floorId)
		DxrContentSource.RETENTION -> {
			val userProfile = DxrSettings.Models.userProfileNotNull
			val userId = userProfile.userIdNotNull

			val floor = DxrRetention.loadFloor(userId, floorId)
			checkNotNull(floor)
		}
	}

	fun holesFlow() = when (DxrSettings.Models.contentSourceOrDefault) {
		DxrContentSource.FORUM_API -> forumApiHolesFlow()
		DxrContentSource.RETENTION -> retentionHolesFlow()
	}

	fun forumApiHolesFlow() = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val sessionState = DxrRetention.loadSessionState(userId)
		var startTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

		val loadLength = 10
		val sortOrder = DxrSettings.Models.sortOrderOrDefault

		while (true) {
			DxrForumApi.ensureAuth()
			val holes = DxrForumApi.loadHoles(
				startTime,
				null,
				length = loadLength.toLong(),
				sortOrder = sortOrder,
			)
			holes.forEach { hole ->
				emit(hole)
				runCatching {
					DxrRetention.retainHole(userId, hole, DxrForumApi::loadHoles)
				}
				hole.tags?.forEach { tag ->
					runCatching {
						DxrRetention.retainTag(userId, tag, DxrForumApi::loadHoles)
					}
				}
				hole.floors?.asList?.forEach { floor ->
					floor ?: return@forEach
					runCatching {
						DxrRetention.retainFloor(userId, floor, DxrForumApi::loadHoles)
					}
				}
				// optional TODO use `forumApiFloorsFlow()` to store full floors
			}
			holes.size < loadLength && break
			startTime = holes.last().getSortingDateTime(sortOrder)
		}
	}

	fun retentionHolesFlow() = run {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val holesSequence = when (DxrSettings.Models.sortOrderOrDefault) {
			SortOrder.LAST_REPLIED -> DxrRetention.loadHolesSequenceByUpdate(userId)
			SortOrder.LAST_CREATED -> DxrRetention.loadHolesSequenceByCreation(userId)
		}
		holesSequence.asFlow()
	}

	fun floorsFlow(holeId: Long) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val holeSessionState = DxrRetention.loadHoleSessionState(userId, holeId)
		val reversed = holeSessionState.reversed == true

		when (DxrSettings.Models.contentSourceOrDefault) {
			DxrContentSource.FORUM_API -> if (reversed) {
				forumApiFloorsReversedFlow(holeId)
			} else {
				forumApiFloorsFlow(holeId)
			}

			DxrContentSource.RETENTION -> if (reversed) {
				retentionFloorsReversedFlow(holeId)
			} else {
				retentionFloorsFlow(holeId)
			}
		}.collect(this)
	}

	fun forumApiFloorsFlow(holeId: Long) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		DxrForumApi.ensureAuth()
		val hole = DxrForumApi.loadHoleById(holeId)

		var startFloorIndex = 0
		val loadLength = 50

		do {
			DxrForumApi.ensureAuth()
			val floors = DxrForumApi.loadFloors(
				hole,
				startFloor = startFloorIndex.toLong(),
				length = loadLength.toLong(),
			)
			floors.forEachIndexed { index, floor ->
				emit(Triple(floor, hole, startFloorIndex + index))
				runCatching {
					DxrRetention.retainFloor(userId, floor, DxrForumApi::loadFloors)
				}
			}
			startFloorIndex += floors.size
		} while (floors.size >= loadLength)
	}

	fun retentionFloorsFlow(holeId: Long) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val hole = loadHole(holeId)
		val floorsSequence = DxrRetention.loadFloorsSequence(userId, holeId)
		floorsSequence.forEachIndexed { index, floor ->
			emit(Triple(floor, hole, index))
		}
	}

	fun forumApiFloorsReversedFlow(holeId: Long): Flow<Triple<OtFloor, OtHole, Int>> {
		return flow {
			DxrForumApi.ensureAuth()
			val hole = DxrForumApi.loadHoleById(holeId)

			var endFloorIndex = hole.floorsCount.toInt()
			val loadLength = 50

			do {
				DxrForumApi.ensureAuth()
				val startFloorIndex = (endFloorIndex - loadLength).coerceAtLeast(0)
				val floors = DxrForumApi.loadFloors(
					hole,
					startFloor = startFloorIndex.toLong(),
					length = (endFloorIndex - startFloorIndex).toLong(),
				).asReversed()
				floors.forEachIndexed { index, floor ->
					emit(Triple(floor, hole, endFloorIndex - index - 1))
				}
				endFloorIndex = startFloorIndex
			} while (endFloorIndex > 0)
		}
	}

	fun retentionFloorsReversedFlow(holeId: Long) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val hole = loadHole(holeId)
		val floorsSequence = DxrRetention.loadFloorsReversedSequence(userId, holeId)
		floorsSequence.forEachIndexed { index, floor ->
			emit(Triple(floor, hole, index))
		}
	}
}
