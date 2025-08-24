package com.buhzzi.danxiretainer.repository.retention

import android.app.Application
import android.content.Context
import android.net.Uri
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrRetentionRequest
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.forum.DxrFloorsFilterContext
import com.buhzzi.danxiretainer.page.forum.DxrHolesFilterContext
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.retentionDeciderOrDefault
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.dxrPrettyJson
import com.buhzzi.danxiretainer.util.floorPathOf
import com.buhzzi.danxiretainer.util.floorsIndicesPathOf
import com.buhzzi.danxiretainer.util.holePathOf
import com.buhzzi.danxiretainer.util.holeSessionStateFilterPathOf
import com.buhzzi.danxiretainer.util.holeSessionStatePathOf
import com.buhzzi.danxiretainer.util.holesIndicesPathOf
import com.buhzzi.danxiretainer.util.sessionStateCurrentPathOf
import com.buhzzi.danxiretainer.util.sessionStateFilterPathOf
import com.buhzzi.danxiretainer.util.tagPathOf
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import com.google.common.collect.TreeRangeSet
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtFloors
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.model.forum.OtTag
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.reflect.KFunction

object DxrRetention {
	private const val FILE_MAGIC_RANGE_SET_I32BE = "DXR\u0001RangeSet\u0001i32be"

	private lateinit var app: Application

	fun init(context: Context) {
		app = context.applicationContext as Application
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun encodeHoleRetainedJson(hole: OtHole, userId: Long): JsonElement? {
		val holeId = hole.holeId ?: return null
		return buildJsonObject {
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
		}
	}

	fun loadHole(userId: Long, holeId: Long): OtHole? {
		val json = readRetainedJson(app.holePathOf(userId, holeId))
		return decodeHoleRetainedJson(json, userId)
	}

	fun decodeHoleRetainedJson(json: JsonElement, userId: Long): OtHole? {
		if (json !is JsonObject) return null
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

	fun storeFloor(userId: Long, floor: OtFloor) {
		val floorId = floor.floorId ?: return
		val json = encodeFloorRetainedJson(floor, userId) ?: return
		writeRetainedJson(app.floorPathOf(userId, floorId), json)
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun encodeFloorRetainedJson(floor: OtFloor, userId: Long): JsonElement? {
		val floorId = floor.floorId ?: return null
		return buildJsonObject {
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
		}
	}

	fun loadFloor(userId: Long, floorId: Long): OtFloor? {
		val json = readRetainedJson(app.floorPathOf(userId, floorId))
		return decodeFloorRetainedJson(json, userId)
	}

	fun decodeFloorRetainedJson(json: JsonElement, userId: Long): OtFloor? {
		if (json !is JsonObject) return null
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

	fun storeTag(userId: Long, tag: OtTag) {
		val tagId = tag.tagId ?: return
		val json = encodeTagRetainedJson(tag) ?: return
		writeRetainedJson(app.tagPathOf(userId, tagId), json)
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun encodeTagRetainedJson(tag: OtTag): JsonElement? {
		val tagId = tag.tagId ?: return null
		return buildJsonObject {
			put("id", tagId)
			put("createdAt", null)
			put("updatedAt", null)
			put("name", tag.name)
			put("isZzmg", null)
			put("isSensitive", null)
			put("isActualSensitive", null)
			put("nsfw", null)

			// Non-existing columns in backend table tag
			put("temperature", tag.temperature)
		}
	}

	fun loadTag(userId: Long, tagId: Long): OtTag? {
		val json = readRetainedJson(app.tagPathOf(userId, tagId))
		return decodeTagRetainedJson(json)
	}

	fun decodeTagRetainedJson(json: JsonElement): OtTag? {
		if (json !is JsonObject) return null
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

	fun updateIndices(indicesPath: Path, update: TreeRangeSet<Int>.() -> RangeSet<Int>) {
		val indices = loadIndices(indicesPath)
		storeIndices(indicesPath, indices.update())
	}

	// TODO clear session states, in `RetentionPage`
	fun storeSessionState(userId: Long, sessionState: DxrSessionState) {
		writeRetainedJson(app.sessionStateCurrentPathOf(userId), dxrJson.encodeToJsonElement(sessionState))
	}

	fun loadSessionState(userId: Long): DxrSessionState {
		val path = app.sessionStateCurrentPathOf(userId)
		if (path.notExists()) {
			path.createParentDirectories().createFile()
		}
		val json = readRetainedJson(path) as? JsonObject ?: return DxrSessionState()
		return dxrJson.decodeFromJsonElement(json)
	}

	fun updateSessionState(userId: Long, update: DxrSessionState.() -> DxrSessionState) {
		val sessionState = loadSessionState(userId)
		storeSessionState(userId, sessionState.update())
	}

	fun storeHoleSessionState(userId: Long, holeId: Long, holeSessionState: DxrHoleSessionState) {
		writeRetainedJson(app.holeSessionStatePathOf(userId, holeId), dxrJson.encodeToJsonElement(holeSessionState))
	}

	fun loadHoleSessionState(userId: Long, holeId: Long): DxrHoleSessionState {
		val path = app.holeSessionStatePathOf(userId, holeId)
		if (path.notExists()) {
			path.createParentDirectories().createFile()
		}
		val json = readRetainedJson(path) as? JsonObject ?: return DxrHoleSessionState()
		return dxrJson.decodeFromJsonElement(json)
	}

	fun updateHoleSessionState(userId: Long, holeId: Long, update: DxrHoleSessionState.() -> DxrHoleSessionState) {
		val holeSessionState = loadHoleSessionState(userId, holeId)
		storeHoleSessionState(userId, holeId, holeSessionState.update())
	}

	fun loadHolesFilterContext(userId: Long): DxrHolesFilterContext {
		val path = app.sessionStateFilterPathOf(userId)
		return DxrHolesFilterContext(path)
	}

	fun loadFloorsFilterContext(userId: Long, holeId: Long): DxrFloorsFilterContext {
		val path = app.holeSessionStateFilterPathOf(userId, holeId)
		return DxrFloorsFilterContext(path)
	}

	/**
	 * Helper function to constrain to the `JsonObject` type.
	 */
	fun storeFilterContextJson(path: Path, filterJson: JsonObject) {
		writeRetainedJson(path, filterJson)
	}

	/**
	 * Helper function to constrain to the `JsonObject` type.
	 */
	fun loadFilterContextJson(path: Path): JsonObject {
		return readRetainedJson(path) as? JsonObject ?: JsonObject(emptyMap())
	}

	fun cacheHttpResource(uri: Uri, cachedPath: Path) {
		cachedPath.exists() && return
		// TODO extract proxied requests as a single part.
		val url = uri.toString().toHttpUrlOrNull() ?: return
		val request = Request.Builder()
			.url(url)
			.get()
			.build()
		val response = DxrForumApi.client.newCall(request)
			.execute()
		response.isSuccessful || throw RuntimeException(response.body.string())
		response.body.byteStream().buffered().use { `in` ->
			cachedPath.createParentDirectories().outputStream().buffered().use { out ->
				`in`.copyTo(out)
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun writeRetainedJson(path: Path, json: JsonElement) {
		path.createParentDirectories()
			.outputStream().buffered()
			.use { out ->
				dxrPrettyJson.encodeToStream(json, out)
			}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun readRetainedJson(path: Path): JsonElement {
		val json = path.takeIf { it.exists() }
			?.inputStream()?.buffered()
			?.use { `in` ->
				runCatching {
					dxrPrettyJson.decodeFromStream<JsonElement>(`in`)
				}.getOrNull()
			}
			?: JsonObject(emptyMap())
		return json
	}

	fun loadHolesSequenceByCreation(userId: Long) = loadDescendingIdsSequence(app.holesIndicesPathOf(userId))
		.mapNotNull { loadHole(userId, it) }

	fun loadHolesSequenceByUpdate(userId: Long) = loadDescendingIdsSequence(app.floorsIndicesPathOf(userId))
		.mapNotNull { loadFloor(userId, it) }
		.mapNotNull { it.holeId }
		.distinct()
		.mapNotNull { loadHole(userId, it) }

	fun loadFloorsSequence(userId: Long, holeId: Long) = loadIncreasingIdsSequence(app.floorsIndicesPathOf(userId))
		.run {
			loadHoleFloorsRange(userId, holeId)
				?.let { range -> filter { it in range } }
				?: this
		}
		.mapNotNull { loadFloor(userId, it) }
		.filter { it.holeId == holeId }

	fun loadFloorsReversedSequence(userId: Long, holeId: Long) = loadDescendingIdsSequence(app.floorsIndicesPathOf(userId))
		.run {
			loadHoleFloorsRange(userId, holeId)
				?.let { range -> filter { it in range } }
				?: this
		}
		.mapNotNull { loadFloor(userId, it) }
		.filter { it.holeId == holeId }

	private fun loadIncreasingIdsSequence(path: Path) = loadIndices(path)
		.asRanges().asSequence()
		.flatMap { it.lowerEndpoint() ..< it.upperEndpoint() }
		.map { it.toLong() }

	private fun loadDescendingIdsSequence(path: Path) = loadIndices(path)
		.asDescendingSetOfRanges().asSequence()
		.flatMap { it.upperEndpoint() - 1 downTo it.lowerEndpoint() }
		.map { it.toLong() }

	private fun loadHoleFloorsRange(userId: Long, holeId: Long) = loadHole(userId, holeId)
		?.floors
		?.runCatching { firstFloorNotNull.floorIdNotNull .. lastFloorNotNull.floorIdNotNull }
		?.getOrNull()

	suspend fun retainHole(userId: Long, hole: OtHole, sourceFunction: KFunction<*>) {
		val retentionDecider = DxrSettings.Models.retentionDeciderOrDefault

		val json = encodeHoleRetainedJson(hole, userId) ?: return
		val holeId = hole.holeId ?: return
		val hasRetained = retentionDecider.tryRetain(
			DxrRetentionRequest.AfterFetchRequest(
				path = app.holePathOf(userId, holeId),
				retention = json,
				function = sourceFunction,
			),
		)
		if (hasRetained) {
			updateIndices(app.holesIndicesPathOf(userId)) {
				add(holeId.toInt().let { Range.closedOpen(it, it + 1) })
				this
			}
		}
	}

	suspend fun retainFloor(userId: Long, floor: OtFloor, sourceFunction: KFunction<*>) {
		val retentionDecider = DxrSettings.Models.retentionDeciderOrDefault

		val json = encodeFloorRetainedJson(floor, userId) ?: return
		val floorId = floor.floorId ?: return
		val hasRetained = retentionDecider.tryRetain(
			DxrRetentionRequest.AfterFetchRequest(
				path = app.floorPathOf(userId, floorId),
				retention = json,
				function = sourceFunction,
			),
		)
		if (hasRetained) {
			updateIndices(app.floorsIndicesPathOf(userId)) {
				add(floorId.toInt().let { Range.closedOpen(it, it + 1) })
				this
			}
		}
	}

	suspend fun retainTag(userId: Long, tag: OtTag, sourceFunction: KFunction<*>) {
		val retentionDecider = DxrSettings.Models.retentionDeciderOrDefault

		val json = encodeTagRetainedJson(tag) ?: return
		val tagId = tag.tagId ?: return
		retentionDecider.tryRetain(
			DxrRetentionRequest.AfterFetchRequest(
				path = app.tagPathOf(userId, tagId),
				retention = json,
				function = sourceFunction,
			),
		)
	}
}
