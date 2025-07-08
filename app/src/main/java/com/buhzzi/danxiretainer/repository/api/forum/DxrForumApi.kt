package com.buhzzi.danxiretainer.repository.api.forum

import android.util.Log
import com.buhzzi.danxiretainer.page.settings.handleJwtAndOptionallyFetchUserProfile
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.accessJwt
import com.buhzzi.danxiretainer.repository.settings.authBaseUrl
import com.buhzzi.danxiretainer.repository.settings.email
import com.buhzzi.danxiretainer.repository.settings.forumBaseUrl
import com.buhzzi.danxiretainer.repository.settings.httpProxy
import com.buhzzi.danxiretainer.repository.settings.imageBaseUrl
import com.buhzzi.danxiretainer.repository.settings.passwordCt
import com.buhzzi.danxiretainer.repository.settings.refreshJwt
import com.buhzzi.danxiretainer.util.androidKeyStoreDecrypt
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.judgeJwtValid
import com.buhzzi.danxiretainer.util.toBytesBase64
import com.buhzzi.danxiretainer.util.toStringRfc3339
import com.buhzzi.danxiretainer.util.toStringUtf8
import dart.dan_xi.common.Constant
import dart.dan_xi.model.forum.JwToken
import dart.dan_xi.model.forum.OtAudit
import dart.dan_xi.model.forum.OtDivision
import dart.dan_xi.model.forum.OtFloor
import dart.dan_xi.model.forum.OtHistory
import dart.dan_xi.model.forum.OtHole
import dart.dan_xi.model.forum.OtMessage
import dart.dan_xi.model.forum.OtPunishment
import dart.dan_xi.model.forum.OtReport
import dart.dan_xi.model.forum.OtTag
import dart.dan_xi.model.forum.OtUser
import dart.dan_xi.model.forum.QuizAnswer
import dart.dan_xi.model.forum.QuizQuestion
import dart.dan_xi.page.KEY_NO_TAG
import dart.dan_xi.provider.SortOrder
import dart.dan_xi.repository.forum.PushNotificationServiceType
import dart.dan_xi.repository.forum.SetStatusMode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.http
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

object DxrForumApi {
	private val baseAuthUrl get() = DxrSettings.Items.authBaseUrl ?: Constant.AUTH_BASE_URL.also {
		DxrSettings.Items.authBaseUrl = it
	}

	private val baseUrl get() = DxrSettings.Items.forumBaseUrl ?: Constant.FORUM_BASE_URL.also {
		DxrSettings.Items.forumBaseUrl = it
	}

	private val baseImageUrl get() = DxrSettings.Items.imageBaseUrl ?: Constant.IMAGE_BASE_URL.also {
		DxrSettings.Items.imageBaseUrl = it
	}

	private val client = HttpClient(CIO) {
		install(ContentNegotiation) {
			json(dxrJson)
		}
		install(Logging) {
			level = LogLevel.ALL
			logger = object : Logger {
				override fun log(message: String) {
					Log.d("CIO", message)
				}
			}
		}
		engine {
			DxrSettings.Models.httpProxy?.takeIf { it.enabled == true }?.runCatching {
				proxy = ProxyBuilder.http("http://${checkNotNull(host)}:${checkNotNull(port)}")
			}
		}
		defaultRequest {
			DxrSettings.Items.accessJwt?.let { accessJwt ->
				if (HttpHeaders.Authorization !in headers) {
					headers[HttpHeaders.Authorization] = "Bearer $accessJwt"
				}
			}
		}
	}

	private suspend fun HttpResponse.checkStatus() {
		if (!status.isSuccess()) {
			throw RuntimeException(bodyAsText())
		}
	}

	private fun OffsetDateTime.toForumString() =
		withOffsetSameInstant(ZoneOffset.ofHours(8)).toStringRfc3339()

	suspend fun authLogIn(email: String, password: String): JwToken {
		val rsp = client.post("$baseAuthUrl/login") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("email", email)
				put("password", password)
			})
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun authRefresh(refreshJwt: String): JwToken {
		val rsp = client.post("$baseAuthUrl/refresh") {
			headers[HttpHeaders.Authorization] = "Bearer $refreshJwt"
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun ensureAuth() {
		val accessJwt = DxrSettings.Items.accessJwt
		val refreshJwt = DxrSettings.Items.refreshJwt

		if (accessJwt?.let { judgeJwtValid(it) } != true) {
			val jwToken = if (refreshJwt?.let { judgeJwtValid(it) } == true) {
				authRefresh(refreshJwt)
			} else {
				authLogIn(
					checkNotNull(DxrSettings.Items.email),
					androidKeyStoreDecrypt(checkNotNull(DxrSettings.Items.passwordCt).toBytesBase64()).toStringUtf8(),
				)
			}
			handleJwtAndOptionallyFetchUserProfile(jwToken, true)
		}
	}

	suspend fun loadDivisions(): List<OtDivision> {
		val rsp = client.get("$baseUrl/divisions")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadSpecificDivision(divisionId: Long): OtDivision {
		val rsp = client.get("$baseUrl/divisions/$divisionId")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadHoles(
		startTime: OffsetDateTime,
		divisionId: Long?,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		tag: String? = null,
		sortOrder: SortOrder = SortOrder.LAST_REPLIED,
	): List<OtHole> {
		val rsp = client.get("$baseUrl/holes") {
			url {
				parameters["start_time"] = startTime.toForumString()
				parameters["division_id"] = (divisionId ?: 0).toString() // 0 = don't filter by division
				parameters["length"] = length.toString()
				parameters["tag"] = tag ?: ""
				parameters["order"] = sortOrder.internalString
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadUserHoles(
		startTime: OffsetDateTime,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		sortOrder: SortOrder = SortOrder.LAST_REPLIED,
	): List<OtHole> {
		val rsp = client.get("$baseUrl/users/me/holes") {
			url {
				parameters["offsets"] = startTime.toForumString()
				parameters["size"] = length.toString()
				parameters["order"] = sortOrder.internalString
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	// NEVER USED
	suspend fun loadHoleById(holeId: Long): OtHole {
		val rsp = client.get("$baseUrl/holes/$holeId")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadFloorById(floorId: Long): OtFloor {
		val rsp = client.get("$baseUrl/floors/$floorId")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadFloors(
		post: OtHole,
		startFloor: Long = 0,
		length: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtFloor> {
		val rsp = client.get("$baseUrl/floors") {
			url {
				parameters["start_floor"] = startFloor.toString()
				parameters["hole_id"] = post.holeId.toString()
				parameters["length"] = length.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadUserFloors(
		startFloor: Long = 0,
		length: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtFloor> {
		val rsp = client.get("$baseUrl/users/me/floors") {
			url {
				parameters["offset"] = startFloor.toString()
				parameters["size"] = length.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadSearchResults(
		searchString: String?,
		startFloor: Long? = null,
		accurate: Boolean = false,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		dateRange: Pair<OffsetDateTime?, OffsetDateTime?> = null to null,
	): List<OtFloor> {
		val rsp = client.get("$baseUrl/floors/search") {
			url {
				parameters["offset"] = startFloor?.toString() ?: ""
				parameters["search"] = searchString ?: ""
				parameters["size"] = length.toString()
				parameters["accurate"] = accurate.toString()
				dateRange.first?.let {
					parameters["start_time"] = it.toEpochSecond().toString()
				}
				dateRange.second?.let {
					parameters["end_time"] = it.toEpochSecond().toString()
				}
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun loadTags(): List<OtTag> {
		val rsp = client.get("$baseUrl/tags")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun newHole(
		divisionId: Long,
		content: String,
		tags: List<OtTag>? = null,
	): Int {
		val tags = tags?.takeIf { it.isNotEmpty() } ?: listOf(OtTag(0, 0, KEY_NO_TAG))
		// Suppose user is logged in. He should be.
		val rsp = client.post("$baseUrl/holes") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("division_id", divisionId)
				put("content", content)
				put("tags", dxrJson.encodeToJsonElement(tags))
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun uploadImage(file: File): String? {
		val rsp = client.post("$baseImageUrl/json") {
			setBody(MultiPartFormDataContent(formData {
				append(
					"source",
					file.readBytes(),
					Headers.Companion.build {
						this[HttpHeaders.ContentDisposition] = "form-data; name=\"source\"; filename=\"${file.name}\""
					}
				)
			}))
		}
		rsp.checkStatus()
		return requireNotNull(rsp.body<JsonObject>()["image"]).jsonObject["display_url"]?.let { dxrJson.decodeFromJsonElement(it) }
	}

	suspend fun newFloor(
		discussionId: Long?,
		content: String,
	): Int {
		val rsp = client.post("$baseUrl/floors") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("content", content)
				put("hole_id", discussionId)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun likeFloor(
		floorId: Long,
		like: Long,
	): OtFloor {
		val rsp = client.post("$baseUrl/floors/$floorId/like/$like")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun reportPost(
		postId: Long?,
		reason: String,
	): Int {
		val rsp = client.post("$baseUrl/reports") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("floor_id", postId)
				put("reason", reason)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun getUserProfile(): OtUser {
		val rsp = client.get("$baseUrl/users/me")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun updateUserProfile(userInfo: OtUser): OtUser {
		val rsp = client.patch("$baseUrl/users/${userInfo.userId}/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(userInfo)
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun updateHoleViewCount(holeId: Long) {
		val rsp = client.patch("$baseUrl/holes/$holeId")
		return rsp.checkStatus()
	}

	suspend fun loadMessages(
		unreadOnly: Boolean = false,
		startTime: OffsetDateTime? = null,
	): List<OtMessage> {
		val rsp = client.get("$baseUrl/messages") {
			url {
				parameters["not_read"] = unreadOnly.toString()
				parameters["start_time"] = startTime?.toForumString() ?: ""
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun modifyMessage(message: OtMessage) {
		val rsp = client.delete("$baseUrl/messages/${message.messageId}") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("has_read", message.hasRead)
			})
		}
		rsp.checkStatus()
	}

	suspend fun clearMessages() {
		val rsp = client.patch("$baseUrl/messages/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("clear_all", true)
			})
		}
		rsp.checkStatus()
	}

	suspend fun getFavoriteHoleId(): List<Long>? {
		val rsp = client.get("$baseUrl/user/favorites") {
			url {
				parameters["plain"] = true.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body<JsonObject>()["data"]?.let { dxrJson.decodeFromJsonElement(it) }
	}

	suspend fun getSubscribedHoleId(): List<Long>? {
		val rsp = client.get("$baseUrl/users/subscriptions") {
			url {
				parameters["plain"] = true.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body<JsonObject>()["data"]?.let { dxrJson.decodeFromJsonElement(it) }
	}

	suspend fun getFavoriteHoles(
		length: Long = Constant.POST_COUNT_PER_PAGE,
		prefetchLength: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtHole> {
		val rsp = client.get("$baseUrl/user/favorites") {
			url {
				parameters["length"] = length.toString()
				parameters["prefetch_length"] = prefetchLength.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun getSubscribedHoles(
		length: Long = Constant.POST_COUNT_PER_PAGE,
		prefetchLength: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtHole> {
		val rsp = client.get("$baseUrl/users/subscriptions") {
			url {
				parameters["length"] = length.toString()
				parameters["prefetch_length"] = prefetchLength.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun setFavorite(
		mode: SetStatusMode,
		holeId: Long?,
	) {
		val rsp = when (mode) {
			SetStatusMode.ADD -> client.post("$baseUrl/user/favorites") {
				contentType(ContentType.Application.Json)
				setBody(buildJsonObject {
					put("hole_id", holeId)
				})
			}
			SetStatusMode.DELETE -> client.delete("$baseUrl/user/favorites") {
				contentType(ContentType.Application.Json)
				setBody(buildJsonObject {
					put("hole_id", holeId)
				})
			}
		}
		rsp.checkStatus()
	}

	suspend fun setSubscription(
		mode: SetStatusMode,
		holeId: Long?,
	) {
		val rsp = when (mode) {
			SetStatusMode.ADD -> client.post("$baseUrl/users/subscriptions") {
				contentType(ContentType.Application.Json)
				setBody(buildJsonObject {
					put("hole_id", holeId)
				})
			}
			SetStatusMode.DELETE -> client.delete("$baseUrl/users/subscriptions") {
				contentType(ContentType.Application.Json)
				setBody(buildJsonObject {
					put("hole_id", holeId)
				})
			}
		}
		rsp.checkStatus()
	}

	/// Modify a floor
	suspend fun modifyFloor(
		content: String,
		floorId: Long?,
	): Int {
		val rsp = client.patch("$baseUrl/floors/$floorId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("content", content)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	/// Delete a floor
	suspend fun deleteFloor(floorId: Long?): Int {
		val rsp = client.delete("$baseUrl/floors/$floorId")
		rsp.checkStatus()
		return rsp.status.value
	}

	/// Get user's punishment history
	suspend fun getPunishmentHistory(): List<OtPunishment> {
		val rsp = client.get("$baseUrl/users/me/punishments")
		rsp.checkStatus()
		return rsp.body()
	}

	/// Get user silence status by floor ID
	/// Returns a map where keys are division IDs and values silence end times
	suspend fun adminGetUserSilenceByFloorId(floorId: Long): Map<String, String> {
		val rsp = client.get("$baseUrl/floors/$floorId/user_silence")
		rsp.checkStatus()
		return rsp.body()
	}

	/// Admin API below
	suspend fun adminGetReports(
		startReport: Long,
		length: Long = 10,
	): List<OtReport> {
		val rsp = client.get("$baseUrl/reports") {
			url {
				parameters["offset"] = startReport.toString()
				parameters["size"] = length.toString()
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun adminGetAuditFloors(
		startTime: OffsetDateTime,
		open: Boolean,
		length: Long = 10,
	): List<OtAudit> {
		val rsp = client.get("$baseUrl/floors/_sensitive") {
			url {
				parameters["offset"] = startTime.toForumString()
				parameters["size"] = length.toString()
				parameters["all"] = false.toString()
				parameters["open"] = open.toString()
				parameters["order_by"] = "time_created"
			}
		}
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun adminSetAuditFloor(
		floorId: Long,
		isActualSensitive: Boolean,
	): Int {
		val rsp = client.patch("$baseUrl/floors/$floorId/_sensitive/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("is_actual_sensitive", isActualSensitive)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminDeleteFloor(
		floorId: Long?,
		deleteReason: String?,
	): Int {
		val rsp = client.delete("$baseUrl/floors/$floorId") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				deleteReason?.let { put("delete_reason", it) }
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminForceDeleteHole(holeId: Long): Int {
		val rsp = client.delete("$baseUrl/holes/$holeId/_force")
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun getHistory(floorId: Long?): List<OtHistory> {
		val rsp = client.get("$baseUrl/floors/$floorId/history")
		rsp.checkStatus()
		return rsp.body()
	}

	suspend fun adminDeleteHole(holeId: Long?): Int {
		val rsp = client.delete("$baseUrl/holes/$holeId")
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminLockHole(
		holeId: Long?,
		lock: Boolean,
	): Int {
		val rsp = client.patch("$baseUrl/holes/$holeId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("lock", lock)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminUndeleteHole(holeId: Long?): Int {
		val rsp = client.patch("$baseUrl/holes/$holeId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("unhidden", true)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	@Deprecated(
		"Use adminAddPenaltyDays instead",
		ReplaceWith("adminAddPenaltyDays(floorId, penaltyDays)"),
	)
	suspend fun adminAddPenalty(
		floorId: Long?,
		penaltyLevel: Long,
	): Int {
		val rsp = client.post("$baseUrl/penalty/$floorId") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("penalty_level", penaltyLevel)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminAddPenaltyDays(
		floorId: Long?,
		penaltyDays: Long,
	): Int {
		val rsp = client.post("$baseUrl/penalty/$floorId") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("days", penaltyDays)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminModifyDivision(
		id: Long,
		name: String?,
		description: String?,
		pinned: List<Long>?,
	): Int {
		val rsp = client.patch("$baseUrl/divisions/$id/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				name?.let { put("name", it) }
				description?.let { put("description", it) }
				pinned?.let { put("pinned", dxrJson.encodeToJsonElement(it)) }
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminAddSpecialTag(
		tag: String,
		floorId: Long?
	): Int {
		val rsp = client.patch("$baseUrl/floors/$floorId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("special_tag", tag)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminUpdateTagAndDivision(
		tag: List<OtTag>,
		holeId: Long?,
		divisionId: Long?,
	): Int {
		val rsp = client.patch("$baseUrl/holes/$holeId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("tags", dxrJson.encodeToJsonElement(tag))
				put("division_id", divisionId)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminFoldFloor(
		fold: List<String>,
		floorId: Long?,
	): Int {
		val rsp = client.patch("$baseUrl/floors/$floorId/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("fold", dxrJson.encodeToJsonElement(fold))
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminChangePassword(
		email: String,
		password: String,
	): Int {
		val rsp = client.patch("$baseUrl/register") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("email", email)
				put("password", password)
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminSendMessage(
		message: String,
		ids: List<Long>,
	): Int {
		val rsp = client.post("$baseUrl/messages") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("content", message)
				put("recipients", dxrJson.encodeToJsonElement(ids))
			})
		}
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminSetReportDealt(reportId: Long): Int {
		val rsp = client.delete("$baseUrl/reports/$reportId")
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun adminGetPunishmentHistory(floorId: Long): List<String> {
		val rsp = client.get("$baseUrl/floors/$floorId/punishment")
		rsp.checkStatus()
		return rsp.body()
	}

	/// Upload or update Push Notification token to server
	suspend fun updatePushNotificationToken(
		token: String,
		id: String,
		service: PushNotificationServiceType,
	) {
		val rsp = client.patch("$baseUrl/users/push-tokens/_webvpn") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("service", service.toStringRepresentation())
				put("device_id", id)
				put("token", token)
			})
		}
		rsp.checkStatus()
	}

	suspend fun deletePushNotificationToken(deviceId: String) {
		val rsp = client.delete("$baseUrl/users/push-tokens") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("device_id", deviceId)
			})
		}
		rsp.checkStatus()
	}

	suspend fun deleteAllPushNotificationToken(): Int {
		val rsp = client.delete("$baseUrl/users/push-tokens/_all")
		rsp.checkStatus()
		return rsp.status.value
	}

	suspend fun getPostRegisterQuestions(): Pair<List<QuizQuestion>?, Int> {
		val rsp = client.get("$baseAuthUrl/register/questions")
		rsp.checkStatus()
		return rsp.body<JsonObject>().run {
			this["questions"]?.let { dxrJson.decodeFromJsonElement<List<QuizQuestion>>(it) } to
				requireNotNull(this["version"]).let { dxrJson.decodeFromJsonElement(it) }
		}
	}

	suspend fun submitAnswers(
		answers: List<QuizAnswer>,
		version: Long,
	): List<Long>? {
		val rsp = client.post("$baseAuthUrl/register/questions/_answer") {
			contentType(ContentType.Application.Json)
			setBody(buildJsonObject {
				put("answers", dxrJson.encodeToJsonElement(answers))
				put("version", version)
			})
		}
		rsp.checkStatus()
		return rsp.body<JsonObject>().run {
			if (this["correct"]?.let { dxrJson.decodeFromJsonElement<Boolean>(it) } == true) {
				listOf()
			} else {
				this["wrong_question_ids"]?.let { dxrJson.decodeFromJsonElement<List<Long>>(it) }
			}
		}
	}
}