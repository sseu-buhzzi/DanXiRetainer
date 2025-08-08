package com.buhzzi.danxiretainer.repository.content

import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.page.forum.DxrFloorsFilterContext
import com.buhzzi.danxiretainer.page.forum.DxrHolesFilterContext
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.content.DxrContent.floorsFlow
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.sortOrderOrDefault
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.package0.dan_xi.model.forum.OtDivision
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.model.forum.OtTag
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

	/**
	 * Need to pass in the [holesFilterContext] because this is a close-and-write-back model.
	 * It fixes the problem that flows cannot read updated [DxrHolesFilterContext] on startup.
	 * The same for [floorsFlow].
	 */
	fun holesFlow(
		holesFilterContext: DxrHolesFilterContext,
	) = when (DxrSettings.Models.contentSourceOrDefault) {
		DxrContentSource.FORUM_API -> forumApiHolesFlow(holesFilterContext)
		DxrContentSource.RETENTION -> retentionHolesFlow(holesFilterContext)
	}

	fun forumApiHolesFlow(
		holesFilterContext: DxrHolesFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val sessionState = DxrRetention.loadSessionState(userId)
		var startTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

		// TODO user division and tag filter as API parameter
		// TODO in multiple flows, everytime fetch the latest

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
			holes.asSequence()
				.filter { hole ->
					holesFilterContext.predicate(hole).also { result ->
						println("#${hole.holeId} $result")
					}
				}
				.forEach { hole ->
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

	fun retentionHolesFlow(
		holesFilterContext: DxrHolesFilterContext,
	) = run {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		when (DxrSettings.Models.sortOrderOrDefault) {
			SortOrder.LAST_REPLIED -> DxrRetention.loadHolesSequenceByUpdate(userId)
			SortOrder.LAST_CREATED -> DxrRetention.loadHolesSequenceByCreation(userId)
		}
			.filter { hole -> holesFilterContext.predicate(hole) }
			.asFlow()
	}

	fun floorsFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val holeSessionState = DxrRetention.loadHoleSessionState(userId, holeId)
		val reversed = holeSessionState.reversed == true

		when (DxrSettings.Models.contentSourceOrDefault) {
			DxrContentSource.FORUM_API -> if (reversed) {
				forumApiFloorsReversedFlow(holeId, floorsFilterContext)
			} else {
				forumApiFloorsFlow(holeId, floorsFilterContext)
			}

			DxrContentSource.RETENTION -> if (reversed) {
				retentionFloorsReversedFlow(holeId, floorsFilterContext)
			} else {
				retentionFloorsFlow(holeId, floorsFilterContext)
			}
		}
			.collect(this)
	}

	fun forumApiFloorsFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
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
			floors.asSequence()
				.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
				.forEachIndexed { index, floor ->
					emit(Triple(floor, hole, startFloorIndex + index))
					runCatching {
						DxrRetention.retainFloor(userId, floor, DxrForumApi::loadFloors)
					}
				}
			startFloorIndex += floors.size
		} while (floors.size >= loadLength)
	}

	fun retentionFloorsFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val hole = loadHole(holeId)
		DxrRetention.loadFloorsSequence(userId, holeId)
			.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
			.forEachIndexed { index, floor ->
				emit(Triple(floor, hole, index))
			}
	}

	fun forumApiFloorsReversedFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	): Flow<Triple<OtFloor, OtHole, Int>> {
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
				floors.asSequence()
					.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
					.forEachIndexed { index, floor ->
						emit(Triple(floor, hole, endFloorIndex - index - 1))
					}
				endFloorIndex = startFloorIndex
			} while (endFloorIndex > 0)
		}
	}

	fun retentionFloorsReversedFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val hole = loadHole(holeId)
		DxrRetention.loadFloorsReversedSequence(userId, holeId)
			.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
			.forEachIndexed { index, floor ->
				emit(Triple(floor, hole, index))
			}
	}

	/// Cached OTDivisions.
	///
	private var divisionsCache: List<OtDivision>? = null

	suspend fun loadDivisions(): List<OtDivision> {
		DxrForumApi.ensureAuth()
		return DxrForumApi.loadDivisions().also { divisionsCache = it }
	}

	/// Cached OTTags.
	private var tagsCache: List<OtTag>? = null

	// TODO load from retention
	suspend fun loadTags(usingCache: Boolean): List<OtTag> {
		tagsCache?.takeIf { usingCache }?.let { return it }
		DxrForumApi.ensureAuth()
		return DxrForumApi.loadTags().also { tagsCache = it }
	}
}
