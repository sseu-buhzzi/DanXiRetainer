package com.buhzzi.danxiretainer.repository.content

import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.page.forum.DxrFloorsFilterContext
import com.buhzzi.danxiretainer.page.forum.DxrHolesFilterContext
import com.buhzzi.danxiretainer.page.settings.handleJwtAndOptionallyFetchUserProfile
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.content.DxrContent.floorsFlow
import com.buhzzi.danxiretainer.repository.content.DxrContent.holesFlow
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.accessJwt
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.email
import com.buhzzi.danxiretainer.repository.settings.passwordCt
import com.buhzzi.danxiretainer.repository.settings.refreshJwt
import com.buhzzi.danxiretainer.repository.settings.sortOrderOrDefault
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.androidKeyStoreDecrypt
import com.buhzzi.danxiretainer.util.judgeJwtValid
import com.buhzzi.danxiretainer.util.toBytesBase64
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringUtf8
import dart.package0.dan_xi.model.forum.OtDivision
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.model.forum.OtTag
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import java.time.OffsetDateTime

object DxrContent {
	fun ensureAuth() {
		val accessJwt = DxrSettings.Prefs.accessJwt
		val refreshJwt = DxrSettings.Prefs.refreshJwt

		if (accessJwt?.let { judgeJwtValid(it) } != true) {
			val jwToken = if (refreshJwt?.let { judgeJwtValid(it) } == true) {
				DxrForumApi.authRefresh(refreshJwt)
			} else {
				DxrForumApi.authLogIn(
					checkNotNull(DxrSettings.Prefs.email),
					androidKeyStoreDecrypt(checkNotNull(DxrSettings.Prefs.passwordCt).toBytesBase64()).toStringUtf8(),
				)
			}
			handleJwtAndOptionallyFetchUserProfile(jwToken, true)
		}
	}

	fun loadHole(holeId: Long): OtHole {
		return when (DxrSettings.Models.contentSourceOrDefault) {
			DxrContentSource.FORUM_API -> {
				ensureAuth()
				DxrForumApi.loadHoleById(holeId)
			}
			DxrContentSource.RETENTION -> {
				val userProfile = DxrSettings.Models.userProfileNotNull
				val userId = userProfile.userIdNotNull

				val hole = DxrRetention.loadHole(userId, holeId)
				checkNotNull(hole)
			}
		}
	}

	fun loadFloor(floorId: Long): OtFloor {
		return when (DxrSettings.Models.contentSourceOrDefault) {
			DxrContentSource.FORUM_API -> {
				ensureAuth()
				DxrForumApi.loadFloorById(floorId)
			}
			DxrContentSource.RETENTION -> {
				val userProfile = DxrSettings.Models.userProfileNotNull
				val userId = userProfile.userIdNotNull

				val floor = DxrRetention.loadFloor(userId, floorId)
				checkNotNull(floor)
			}
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
		DxrContentSource.FORUM_API -> forumApiAdHocHolesFlow(holesFilterContext)
		DxrContentSource.RETENTION -> retentionHolesFlow(holesFilterContext)
	}

	fun forumApiAdHocHolesFlow(
		holesFilterContext: DxrHolesFilterContext,
	): Flow<OtHole> = flow {
		coroutineScope {
			val holesFilterContextJson = holesFilterContext.json
			val divisionIds = (holesFilterContextJson["division"] as? JsonArray)
				?.mapNotNull { (it as? JsonPrimitive)?.intOrNull }
				?.takeIf { it.isNotEmpty() }
				?: listOf(0)
			val tagLabels = (holesFilterContextJson["tag"] as? JsonArray)
				?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
				?.takeIf { it.isNotEmpty() }
				?: listOf(null)
			val sortOrder = DxrSettings.Models.sortOrderOrDefault
			val holesChannels = divisionIds.flatMap { divisionId ->
				tagLabels.map { tagLabel ->
					@OptIn(ExperimentalCoroutinesApi::class)
					produce {
						forumApiHolesFlow(divisionId, tagLabel, holesFilterContext).collect { hole ->
							send(hole)
						}
					}
				}
			}
			try {
				val holesBuffer = holesChannels.map { channel ->
					channel.receiveCatching().getOrNull()
				}.toMutableList()
				while (true) {
					val (newestIndex, newestHole) = holesBuffer.asSequence()
						.withIndex()
						.mapNotNull { (index, hole) -> hole?.let { IndexedValue(index, hole) } }
						.maxByOrNull { (_, hole) -> hole.getSortingDateTime(sortOrder) }
						?: break
					emit(newestHole)
					holesBuffer[newestIndex] = holesChannels[newestIndex].receiveCatching().getOrNull()
				}
			} finally {
				holesChannels.forEach { channel -> channel.cancel() }
			}
		}
	}

	fun forumApiHolesFlow(
		divisionId: Int?,
		tagLabel: String?,
		holesFilterContext: DxrHolesFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val sessionState = DxrRetention.loadSessionState(userId)
		var startTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

		val loadLength = 10
		val sortOrder = DxrSettings.Models.sortOrderOrDefault

		while (true) {
			ensureAuth()
			val holes = DxrForumApi.loadHoles(
				startTime,
				divisionId?.toLong(),
				length = loadLength.toLong(),
				tag = tagLabel,
				sortOrder = sortOrder,
			)
			holes.asSequence()
				.filter { hole -> holesFilterContext.predicate(hole) }
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

	/**
	 * For the reason why it needs [floorsFilterContext], see [holesFlow].
	 */
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

		ensureAuth()
		val hole = DxrForumApi.loadHoleById(holeId)

		var startFloorIndex = 0
		val loadLength = 50

		do {
			// We cannot ensure it is always in 30 minutes after last authentication. As it is in a flow, users may leave their
			// phones away and come back some time.
			ensureAuth()
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

		val hole = checkNotNull(DxrRetention.loadHole(userId, holeId))
		DxrRetention.loadFloorsSequence(userId, holeId)
			.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
			.forEachIndexed { index, floor ->
				emit(Triple(floor, hole, index))
			}
	}

	fun forumApiFloorsReversedFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
		ensureAuth()
		val hole = DxrForumApi.loadHoleById(holeId)

		var endFloorIndex = hole.floorsCount.toInt()
		val loadLength = 50

		do {
			// We cannot ensure it is always in 30 minutes after last authentication. As it is in a flow, users may leave their
			// phones away and come back some time.
			ensureAuth()
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

	fun retentionFloorsReversedFlow(
		holeId: Long,
		floorsFilterContext: DxrFloorsFilterContext,
	) = flow {
		val userProfile = DxrSettings.Models.userProfileNotNull
		val userId = userProfile.userIdNotNull

		val hole = checkNotNull(DxrRetention.loadHole(userId, holeId))
		DxrRetention.loadFloorsReversedSequence(userId, holeId)
			.filter { (floor, _, _) -> floorsFilterContext.predicate(floor) }
			.forEachIndexed { index, floor ->
				emit(Triple(floor, hole, index))
			}
	}

	/// Cached OTDivisions.
	///
	private var divisionsCache: List<OtDivision>? = null

	fun loadDivisions(): List<OtDivision> {
		return divisionsCache ?: run {
			ensureAuth()
			DxrForumApi.loadDivisions().also { divisionsCache = it }
		}
	}

	/// Cached OTTags.
	private var tagsCache: List<OtTag>? = null

	// TODO load from retention
	fun loadTags(usingCache: Boolean): List<OtTag> {
		return tagsCache?.takeIf { usingCache } ?: run {
			ensureAuth()
			DxrForumApi.loadTags().also { tagsCache = it }
		}
	}
}
