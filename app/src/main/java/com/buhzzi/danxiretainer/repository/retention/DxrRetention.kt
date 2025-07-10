package com.buhzzi.danxiretainer.repository.retention

import android.app.Application
import android.content.Context
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.dxrPrettyJson
import com.buhzzi.danxiretainer.util.escapeTsv
import com.buhzzi.danxiretainer.util.floorIndicesPathOf
import com.buhzzi.danxiretainer.util.floorPathOf
import com.buhzzi.danxiretainer.util.holeIndicesPathOf
import com.buhzzi.danxiretainer.util.holePathOf
import com.buhzzi.danxiretainer.util.holeSessionStatePathOf
import com.buhzzi.danxiretainer.util.sessionStateCurrentPathOf
import com.buhzzi.danxiretainer.util.tagPathOf
import com.buhzzi.danxiretainer.util.tagsDirPathOf
import com.buhzzi.danxiretainer.util.unescapeTsv
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtFloors
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.model.forum.OtTag
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Path
import java.time.OffsetDateTime
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.io.path.readLines

object DxrRetention {
	private const val FILE_MAGIC_RANGE_SET_I32BE = "DXR\u0001RangeSet\u0001i32be"

	private lateinit var app: Application

	fun init(context: Context) {
		app = context.applicationContext as Application
	}

	suspend fun storeForExample() {
		DxrForumApi.ensureAuth()

		// TODO 各調用參數
		val holesCountPerLoad = 10
		val floorsCountPerLoad = 50
		val holes = DxrForumApi.loadHoles(
			OffsetDateTime.now(),
			null,
			length = holesCountPerLoad.toLong(),
			tag = null,
			sortOrder = SortOrder.LAST_REPLIED,
		)
		val shouldUsePrefetch = true
		val floors = if (shouldUsePrefetch) {
			holes.flatMap { hole ->
				buildList {
					hole.floors?.run {
						firstFloor?.let { add(it) }
						lastFloor?.let { add(it) }
					}
				}
			}
		} else {
			holes.flatMap { hole ->
				buildList {
					var floorsLoaded: List<OtFloor>
					do {
						floorsLoaded = DxrForumApi.loadFloors(
							hole,
							size.toLong(),
							floorsCountPerLoad.toLong(),
						)
						addAll(floorsLoaded)
					} while (floorsLoaded.size == floorsCountPerLoad)
				}
			}
		}

		val userId = checkNotNull(DxrSettings.Models.userProfile) { "No user profile" }.userIdNotNull
		storeHolesAndUpdateIndices(userId, holes)
		storeFloorsAndUpdateIndices(userId, floors)
		app.tagsDirPathOf(userId).createDirectories()
		holes.asSequence()
			.flatMap { it.tags ?: emptyList() }
			.distinct()
			.forEach { storeTag(userId, it) }
	}

	fun storeHolesAndUpdateIndices(userId: Long, holes: List<OtHole>) {
		val indicesPath = app.holeIndicesPathOf(userId)
		loadIndices(indicesPath).apply {
			holes.forEach { hole ->
				storeHole(userId, hole)
				add(hole.holeIdNotNull.toInt().let { Range.closedOpen(it, it + 1) })
			}
		}.let { storeIndices(indicesPath, it) }
	}

	fun storeFloorsAndUpdateIndices(userId: Long, floors: List<OtFloor>) {
		val indicesPath = app.floorIndicesPathOf(userId)
		loadIndices(indicesPath).apply {
			floors.forEach { floor ->
				storeFloor(userId, floor)
				add(floor.floorIdNotNull.toInt().let { Range.closedOpen(it, it + 1) })
			}
		}.let { storeIndices(indicesPath, it) }
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun storeHole(userId: Long, hole: OtHole) {
		val holeId = hole.holeId ?: return
		writeRetainedJson(
			app.holePathOf(userId, holeId),
			buildJsonObject {
				put("id", holeId)
				put("created_at", hole.timeCreated)
				put("updated_at", hole.timeUpdated)
				put("deleted_at", hole.timeDeleted)
				put("view", hole.view)
				put("reply", hole.reply)
				put("hidden", hole.hidden)
				put("locked", hole.locked)
				put("locked", hole.locked)
				put("good", null)
				put("no_purge", null)
				put("division_id", hole.divisionId)
				put("user_id", userId.takeIf { hole.floors?.firstFloor?.isMe == true })

				// Non-existing columns in backend table hole
				putJsonArray("tags") {
					hole.tags?.mapNotNull { it.tagId }?.let { addAll(it) }
				}
				putJsonArray(
					"floors",
				) {
					hole.floors?.run {
						listOf(firstFloor, lastFloor).mapNotNull { dxrJson.encodeToJsonElement(it?.floorId) }
					}?.let { addAll(it) }
				}
				// Non-existing columns in backend table hole
				put("tags", dxrJson.encodeToJsonElement(hole.tags?.mapNotNull { it.tagId }))
				put("floors", dxrJson.encodeToJsonElement(hole.floors?.run {
					listOf(firstFloor, lastFloor).map { dxrJson.encodeToJsonElement(it?.floorId) }
				}))
			},
		)
	}

	fun loadHole(userId: Long, holeId: Long): OtHole? {
		val json = readRetainedJson(app.holePathOf(userId, holeId)) as? JsonObject ?: return null
		return OtHole(
			holeId = json["id"]?.let { dxrJson.decodeFromJsonElement(it) },
			divisionId = json["division_id"]?.let { dxrJson.decodeFromJsonElement(it) },
			timeCreated = json["created_at"]?.let { dxrJson.decodeFromJsonElement(it) },
			timeUpdated = json["updated_at"]?.let { dxrJson.decodeFromJsonElement(it) },
			timeDeleted = json["deleted_at"]?.let { dxrJson.decodeFromJsonElement(it) },
			// TODO 从本地存儲獲取
			tags = json["tags"]?.let { dxrJson.decodeFromJsonElement<List<Long?>?>(it) }?.mapNotNull { tagId ->
				tagId?.let { loadTag(userId, it) }
			},
			view = json["view"]?.let { dxrJson.decodeFromJsonElement(it) },
			reply = json["reply"]?.let { dxrJson.decodeFromJsonElement(it) },
			// TODO 从本地存儲獲取
			floors = json["floors"]?.let { dxrJson.decodeFromJsonElement<List<Long?>?>(it) }
				?.let { (firstFloorId, lastFloorId) ->
					OtFloors(
						firstFloor = firstFloorId?.let { loadFloor(userId, it) },
						lastFloor = lastFloorId?.let { loadFloor(userId, it) },
					)
				},
			hidden = json["hidden"]?.let { dxrJson.decodeFromJsonElement(it) },
			locked = json["locked"]?.let { dxrJson.decodeFromJsonElement(it) },
		)
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun storeFloor(userId: Long, floor: OtFloor) {
		val floorId = floor.floorId ?: return
		writeRetainedJson(
			app.floorPathOf(userId, floorId),
			buildJsonObject {
				put("id", floorId)
				put("created_at", floor.timeCreated)
				put("updated_at", floor.timeUpdated)
				put("content", floor.content)
				put("anonyname", floor.anonyname)
				// TODO 加入樓層
				put("ranking", null)
				// TODO 加入回覆、或預設空
				put("replyTo", null)
				put("like", floor.like)
				put("dislike", floor.dislike)
				put("deleted", floor.deleted)
				put("modified", floor.modified)
				put("fold", dxrJson.encodeToJsonElement(floor.fold))
				put("special_tag", floor.specialTag)
				put("is_sensitive", null)
				put("is_actual_sensitive", null)
				put("sensitive_detail", null)
				put("user_id", userId.takeIf { floor.isMe == true })
				put("hole_id", floor.holeId)

				// Non-existing columns in backend table floor
				// TODO 不存在之列
				put("liked", floor.liked)
				put("disliked", floor.disliked)
			},
		)
	}

	fun loadFloor(userId: Long, floorId: Long): OtFloor? {
		val json = readRetainedJson(app.floorPathOf(userId, floorId)) as? JsonObject ?: return null
		return OtFloor(
			floorId = json["id"]?.let { dxrJson.decodeFromJsonElement(it) },
			holeId = json["hole_id"]?.let { dxrJson.decodeFromJsonElement(it) },
			content = json["content"]?.let { dxrJson.decodeFromJsonElement(it) },
			anonyname = json["anonyname"]?.let { dxrJson.decodeFromJsonElement(it) },
			timeCreated = json["created_at"]?.let { dxrJson.decodeFromJsonElement(it) },
			timeUpdated = json["updated_at"]?.let { dxrJson.decodeFromJsonElement(it) },
			deleted = json["deleted"]?.let { dxrJson.decodeFromJsonElement(it) },
			fold = json["fold"]?.let { dxrJson.decodeFromJsonElement(it) },
			like = json["like"]?.let { dxrJson.decodeFromJsonElement(it) },
			isMe = json["user_id"]?.let { dxrJson.decodeFromJsonElement<Long?>(it) } == userId,
			// TODO like存儲表
			liked = json["liked"]?.let { dxrJson.decodeFromJsonElement(it) },
			mention = null,
			dislike = json["dislike"]?.let { dxrJson.decodeFromJsonElement(it) },
			disliked = json["disliked"]?.let { dxrJson.decodeFromJsonElement(it) },
			specialTag = json["special_tag"]?.let { dxrJson.decodeFromJsonElement(it) },
			modified = json["modified"]?.let { dxrJson.decodeFromJsonElement(it) },
		)
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun storeTag(userId: Long, tag: OtTag) {
		val tagId = tag.tagId ?: return
		writeRetainedJson(
			app.tagPathOf(userId, tagId),
			buildJsonObject {
				put("id", tag.tagId)
				put("createdAt", null)
				put("updatedAt", null)
				put("name", tag.name)
				put("isZzmg", null)
				put("isSensitive", null)
				put("isActualSensitive", null)
				put("nsfw", null)

				// Non-existing columns in backend table hole
				put("temperature", tag.temperature)
			},
		)
	}

	fun loadTag(userId: Long, tagId: Long): OtTag? {
		val json = readRetainedJson(app.tagPathOf(userId, tagId)) as? JsonObject ?: return null
		return OtTag(
			tagId = json["id"]?.let { dxrJson.decodeFromJsonElement(it) },
			temperature = json["temperature"]?.let { dxrJson.decodeFromJsonElement(it) },
			name = json["name"]?.let { dxrJson.decodeFromJsonElement(it) },
		)
	}

	fun storeIndices(
		indicesPath: Path,
		indices: RangeSet<Int>,
	) = DataOutputStream(indicesPath.outputStream()).use { dataOut ->
		dataOut.writeUTF(FILE_MAGIC_RANGE_SET_I32BE)
		val indexRanges = indices.asRanges()
		dataOut.writeInt(indexRanges.size)
		indexRanges.forEach { range ->
			dataOut.writeInt(range.lowerEndpoint())
			dataOut.writeInt(range.upperEndpoint())
		}
	}

	fun loadIndices(indicesPath: Path): TreeRangeSet<Int> = TreeRangeSet.create<Int>().apply {
		if (indicesPath.notExists()) {
			indicesPath.createParentDirectories().createFile()
		}
		runCatching {
			DataInputStream(indicesPath.inputStream()).use { dataIn ->
				check(dataIn.readUTF() == FILE_MAGIC_RANGE_SET_I32BE)
				val indexRangesCount = dataIn.readInt()
				repeat(indexRangesCount) {
					val lower = dataIn.readInt()
					val upper = dataIn.readInt()
					add(Range.closedOpen(lower, upper))
				}
			}
		}
	}

	// TODO clear session states, in `RetentionPage`
	fun storeSessionState(userId: Long, sessionState: DxrSessionState) {
		writeRetainedJson(app.sessionStateCurrentPathOf(userId), dxrJson.encodeToJsonElement(sessionState))
	}

	fun loadSessionState(userId: Long): DxrSessionState? {
		val path = app.sessionStateCurrentPathOf(userId)
		if (path.notExists()) {
			path.createParentDirectories().createFile()
		}
		val json = readRetainedJson(path) as? JsonObject ?: return null
		return dxrJson.decodeFromJsonElement(json)
	}

	fun updateSessionState(userId: Long, update: DxrSessionState.() -> DxrSessionState) {
		val sessionState = loadSessionState(userId) ?: DxrSessionState()
		storeSessionState(userId, sessionState.update())
	}

	fun storeHoleSessionState(userId: Long, holeId: Long, holeSessionState: DxrHoleSessionState) {
		writeRetainedJson(app.holeSessionStatePathOf(userId, holeId), dxrJson.encodeToJsonElement(holeSessionState))
	}

	fun loadHoleSessionState(userId: Long, holeId: Long): DxrHoleSessionState? {
		val path = app.holeSessionStatePathOf(userId, holeId)
		if (path.notExists()) {
			path.createParentDirectories().createFile()
		}
		val json = readRetainedJson(path) as? JsonObject ?: return null
		return dxrJson.decodeFromJsonElement(json)
	}

	fun updateHoleSessionState(userId: Long, holeId: Long, update: DxrHoleSessionState.() -> DxrHoleSessionState) {
		val holeSessionState = loadHoleSessionState(userId, holeId) ?: DxrHoleSessionState()
		storeHoleSessionState(userId, holeId, holeSessionState.update())
	}

	private fun writeRetainedTsv(path: Path, entries: Sequence<Pair<String, Any?>>) = path.bufferedWriter().use { writer ->
		entries.forEach { (key, value) ->
			writer.write(key.escapeTsv())
			writer.write('\t'.code)
			writer.write(value?.toString()?.escapeTsv() ?: "")
			writer.write('\n'.code)
		}
	}

	private fun readRetainedTsv(path: Path) = path.takeIf { it.exists() }
		?.readLines()?.asSequence()
		?.map { line -> line.split('\t', limit = 2) }
		?.associate { (key, value) -> key.unescapeTsv() to value.unescapeTsv() }
		?: emptyMap()

	@OptIn(ExperimentalSerializationApi::class)
	private fun writeRetainedJson(path: Path, json: JsonElement) = path.createParentDirectories()
		.outputStream().buffered()
		.use { out ->
			dxrPrettyJson.encodeToStream(json, out)
		}

	@OptIn(ExperimentalSerializationApi::class)
	private fun readRetainedJson(path: Path) = path.takeIf { it.exists() }
		?.inputStream()?.buffered()
		?.use { `in` ->
			runCatching {
				dxrPrettyJson.decodeFromStream<JsonElement>(`in`)
			}.getOrNull()
		}
		?: buildJsonObject { }

	fun loadHoleSequenceByCreation(userId: Long) = loadDescendingIdSequence(app.holeIndicesPathOf(userId))
		.mapNotNull { loadHole(userId, it.toLong()) }

	fun loadHoleSequenceByUpdate(userId: Long) = loadDescendingIdSequence(app.floorIndicesPathOf(userId))
		.mapNotNull { loadFloor(userId, it.toLong()) }
		.mapNotNull { it.holeId }
		.distinct()
		.mapNotNull { loadHole(userId, it.toLong()) }

	// TODO 以預加載OtHole縮小firstFloor到lastFloor間者範圍
	fun loadFloorSequence(userId: Long, holeId: Long) = loadIncreasingIdSequence(app.floorIndicesPathOf(userId))
		.mapNotNull { loadFloor(userId, it.toLong()) }
		.filter { it.holeId == holeId }

	// TODO 以預加載OtHole縮小firstFloor到lastFloor間者範圍
	fun loadFloorSequenceReversed(userId: Long, holeId: Long) = loadDescendingIdSequence(app.floorIndicesPathOf(userId))
		.mapNotNull { loadFloor(userId, it.toLong()) }
		.filter { it.holeId == holeId }

	private fun loadIncreasingIdSequence(path: Path) = loadIndices(path)
		.asRanges().asSequence()
		.flatMap { it.lowerEndpoint() ..< it.upperEndpoint() }

	private fun loadDescendingIdSequence(path: Path) = loadIndices(path)
		.asDescendingSetOfRanges().asSequence()
		.flatMap { it.upperEndpoint() - 1 downTo it.lowerEndpoint() }
}