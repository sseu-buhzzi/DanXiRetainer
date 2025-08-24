package com.buhzzi.danxiretainer.repository.api.forum

import android.util.Log
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.accessJwt
import com.buhzzi.danxiretainer.repository.settings.authBaseUrlOrDefault
import com.buhzzi.danxiretainer.repository.settings.forumBaseUrlOrDefault
import com.buhzzi.danxiretainer.repository.settings.httpProxy
import com.buhzzi.danxiretainer.repository.settings.imageBaseUrlOrDefault
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.toStringRfc3339
import dart.package0.dan_xi.common.Constant
import dart.package0.dan_xi.model.forum.JwToken
import dart.package0.dan_xi.model.forum.OtAudit
import dart.package0.dan_xi.model.forum.OtDivision
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHistory
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.model.forum.OtMessage
import dart.package0.dan_xi.model.forum.OtPunishment
import dart.package0.dan_xi.model.forum.OtReport
import dart.package0.dan_xi.model.forum.OtTag
import dart.package0.dan_xi.model.forum.OtUser
import dart.package0.dan_xi.model.forum.QuizAnswer
import dart.package0.dan_xi.model.forum.QuizQuestion
import dart.package0.dan_xi.page.KEY_NO_TAG
import dart.package0.dan_xi.provider.SortOrder
import dart.package0.dan_xi.repository.forum.PushNotificationServiceType
import dart.package0.dan_xi.repository.forum.SetStatusMode
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.OffsetDateTime
import java.time.ZoneOffset

object DxrForumApi {
	private val baseAuthUrl get() = DxrSettings.Models.authBaseUrlOrDefault
	private val baseUrl get() = DxrSettings.Models.forumBaseUrlOrDefault
	private val baseImageUrl get() = DxrSettings.Models.imageBaseUrlOrDefault

	private val applicationJsonMediaType = "application/json".toMediaType()

	lateinit var client: OkHttpClient
		private set

	init {
		updateClient()
	}

	fun updateClient() {
		client = OkHttpClient.Builder()
			.proxy(DxrSettings.Models.httpProxy?.takeIf { it.enabled == true }?.runCatching {
				Proxy(Proxy.Type.HTTP, InetSocketAddress(hostNotNull, portNotNull))
			}?.getOrNull())
			.addInterceptor(HttpLoggingInterceptor { message ->
				Log.d("OkHttp", message)
			}.setLevel(HttpLoggingInterceptor.Level.BODY))
			.addInterceptor { chain ->
				val originalRequest = chain.request()
				val request = DxrSettings.Prefs.accessJwt
					?.takeIf { originalRequest.header(HttpHeaders.Authorization) == null }
					?.let { accessJwt ->
						originalRequest.newBuilder()
							.addHeader(HttpHeaders.Authorization, "Bearer $accessJwt")
							.build()
					}
					?: originalRequest
				chain.proceed(request)
			}
			.build()
	}

	private class QueryParametersBuilder(base: String) {
		val builder = base.toHttpUrl().newBuilder()
		fun add(name: String, value: String?) {
			builder.addQueryParameter(name, value)
		}
	}

	private inline fun <reified T> get(
		url: HttpUrl,
		crossinline headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.get()
		.response<T>()

	private fun getWithCode(
		url: HttpUrl,
		headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.get()
		.responseCode()

	private inline fun <reified T> post(
		url: HttpUrl,
		body: JsonElement,
		crossinline headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.post(body.encodeBody())
		.response<T>()

	private fun postWithCode(
		url: HttpUrl,
		body: JsonElement,
		headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.post(body.encodeBody())
		.responseCode()

	private inline fun <reified T> patch(
		url: HttpUrl,
		body: JsonElement,
		crossinline headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.patch(body.encodeBody())
		.response<T>()

	private fun patchWithCode(
		url: HttpUrl,
		body: JsonElement,
		headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.patch(body.encodeBody())
		.responseCode()

	private inline fun <reified T> delete(
		url: HttpUrl,
		body: JsonElement,
		crossinline headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.delete(body.encodeBody())
		.response<T>()

	private fun deleteWithCode(
		url: HttpUrl,
		body: JsonElement,
		headersBuilder: Headers.Builder.() -> Unit = { },
	) = request(url) { headersBuilder() }
		.delete(body.encodeBody())
		.responseCode()

	private fun url(base: String, queryParametersBuilder: QueryParametersBuilder.() -> Unit = { }) =
		QueryParametersBuilder(base).run {
			queryParametersBuilder()
			builder.build()
		}

	private fun request(url: HttpUrl, headersBuilder: Headers.Builder.() -> Unit = { }) = Request.Builder()
		.url(url)
		.headers(Headers.Builder().run {
			headersBuilder()
			build()
		})

	private inline fun <reified T> Request.Builder.response() = client.newCall(build())
		.execute()
		.checkStatus()
		.body
		.decodeBody<T>()

	private fun Request.Builder.responseCode() = client.newCall(build())
		.execute()
		.checkStatus()
		.code

	private inline fun <reified T> T.encodeBody() =
		dxrJson.encodeToString(this).toRequestBody(applicationJsonMediaType)

	private inline fun <reified T> ResponseBody.decodeBody() =
		dxrJson.decodeFromString<T>(string())

	private fun Response.checkStatus() = apply {
		isSuccessful || throw RuntimeException(body.string())
	}

	private fun OffsetDateTime.toForumString() =
		withOffsetSameInstant(ZoneOffset.ofHours(8)).toStringRfc3339()

	fun authLogIn(email: String, password: String): JwToken {
		return post("$baseAuthUrl/login".toHttpUrl(), buildJsonObject {
			put("email", email)
			put("password", password)
		})
	}

	fun authRefresh(refreshJwt: String): JwToken {
		return post(url("$baseAuthUrl/refresh"), JsonObject(emptyMap())) {
			add(HttpHeaders.Authorization, "Bearer $refreshJwt")
		}
	}

	fun loadDivisions(): List<OtDivision> {
		return get(url("$baseUrl/divisions"))
	}

	fun loadSpecificDivision(divisionId: Long): OtDivision {
		return get(url("$baseUrl/divisions/$divisionId"))
	}

	fun loadHoles(
		startTime: OffsetDateTime,
		divisionId: Long?,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		tag: String? = null,
		sortOrder: SortOrder = SortOrder.LAST_REPLIED,
	): List<OtHole> {
		return get(url("$baseUrl/holes") {
			add("start_time", startTime.toForumString())
			add("division_id", (divisionId ?: 0).toString()) // 0 = don't filter by division
			add("length", length.toString())
			add("tag", tag ?: "")
			add("order", sortOrder.internalString)
		})
	}

	fun loadUserHoles(
		startTime: OffsetDateTime,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		sortOrder: SortOrder = SortOrder.LAST_REPLIED,
	): List<OtHole> {
		return get(url("$baseUrl/users/me/holes") {
			add("offsets", startTime.toForumString())
			add("size", length.toString())
			add("order", sortOrder.internalString)
		})
	}

	// NEVER USED
	fun loadHoleById(holeId: Long): OtHole {
		return get(url("$baseUrl/holes/$holeId"))
	}

	fun loadFloorById(floorId: Long): OtFloor {
		return get(url("$baseUrl/floors/$floorId"))
	}

	fun loadFloors(
		post: OtHole,
		startFloor: Long = 0,
		length: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtFloor> {
		return get(url("$baseUrl/floors") {
			add("start_floor", startFloor.toString())
			add("hole_id", post.holeId.toString())
			add("length", length.toString())
		})
	}

	fun loadUserFloors(
		startFloor: Long = 0,
		length: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtFloor> {
		return get(url("$baseUrl/users/me/floors") {
			add("offset", startFloor.toString())
			add("size", length.toString())
		})
	}

	fun loadSearchResults(
		searchString: String?,
		startFloor: Long? = null,
		accurate: Boolean = false,
		length: Long = Constant.POST_COUNT_PER_PAGE,
		dateRange: Pair<OffsetDateTime?, OffsetDateTime?> = null to null,
	): List<OtFloor> {
		return get(url("$baseUrl/floors/search") {
			add("offset", startFloor?.toString() ?: "")
			add("search", searchString ?: "")
			add("size", length.toString())
			add("accurate", accurate.toString())
			dateRange.first?.let {
				add("start_time", it.toEpochSecond().toString())
			}
			dateRange.second?.let {
				add("end_time", it.toEpochSecond().toString())
			}
		})
	}

	fun loadTags(): List<OtTag> {
		return get(url("$baseUrl/tags"))
	}

	fun newHole(
		divisionId: Long,
		content: String,
		tags: List<OtTag>? = null,
	): Int {
		val tags = tags?.takeIf { it.isNotEmpty() } ?: listOf(OtTag(0, 0, KEY_NO_TAG))
		// Suppose user is logged in. He should be.
		return postWithCode(url("$baseUrl/holes"), buildJsonObject {
			put("division_id", divisionId)
			put("content", content)
			put("tags", dxrJson.encodeToJsonElement(tags))
		})
	}

	fun uploadImage(file: File): String? {
		val body = MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("source", file.name, file.asRequestBody())
			.build()
		return request(url("$baseImageUrl/json"))
			.post(body)
			.response<JsonObject>()
			.run { checkNotNull(this["image"]) }
			.jsonObject["display_url"]
			?.let { dxrJson.decodeFromJsonElement(it) }
	}

	fun newFloor(
		discussionId: Long?,
		content: String,
	): Int {
		return postWithCode(url("$baseUrl/floors"), buildJsonObject {
			put("content", content)
			put("hole_id", discussionId)
		})
	}

	fun likeFloor(
		floorId: Long,
		like: Long,
	): OtFloor {
		return post(url("$baseUrl/floors/$floorId/like/$like"), JsonObject(emptyMap()))
	}

	fun reportPost(
		postId: Long?,
		reason: String,
	): Int {
		return postWithCode(url("$baseUrl/reports"), buildJsonObject {
			put("floor_id", postId)
			put("reason", reason)
		})
	}

	fun getUserProfile(): OtUser {
		return get(url("$baseUrl/users/me"))
	}

	fun updateUserProfile(userInfo: OtUser): OtUser {
		return patch(url("$baseUrl/users/${userInfo.userId}/_webvpn"), dxrJson.encodeToJsonElement(userInfo))
	}

	fun updateHoleViewCount(holeId: Long) {
		return patch(url("$baseUrl/holes/$holeId"), JsonObject(emptyMap()))
	}

	fun loadMessages(
		unreadOnly: Boolean = false,
		startTime: OffsetDateTime? = null,
	): List<OtMessage> {
		return get(url("$baseUrl/messages") {
			add("not_read", unreadOnly.toString())
			add("start_time", startTime?.toForumString() ?: "")
		})
	}

	fun modifyMessage(message: OtMessage) {
		deleteWithCode(url("$baseUrl/messages/${message.messageId}"), buildJsonObject {
			put("has_read", message.hasRead)
		})
	}

	fun clearMessages() {
		patchWithCode(url("$baseUrl/messages/_webvpn"), buildJsonObject {
			put("clear_all", true)
		})
	}

	fun getFavoriteHoleId(): List<Long>? {
		return get<JsonObject>(url("$baseUrl/user/favorites") {
			add("plain", true.toString())
		})["data"]?.let { dxrJson.decodeFromJsonElement(it) }
	}

	fun getSubscribedHoleId(): List<Long>? {
		return get<JsonObject>(url("$baseUrl/users/subscriptions") {
			add("plain", true.toString())
		})["data"]?.let { dxrJson.decodeFromJsonElement(it) }
	}

	fun getFavoriteHoles(
		length: Long = Constant.POST_COUNT_PER_PAGE,
		prefetchLength: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtHole> {
		return get(url("$baseUrl/user/favorites") {
			add("length", length.toString())
			add("prefetch_length", prefetchLength.toString())
		})
	}

	fun getSubscribedHoles(
		length: Long = Constant.POST_COUNT_PER_PAGE,
		prefetchLength: Long = Constant.POST_COUNT_PER_PAGE,
	): List<OtHole> {
		return get(url("$baseUrl/users/subscriptions") {
			add("length", length.toString())
			add("prefetch_length", prefetchLength.toString())
		})
	}

	fun setFavorite(
		mode: SetStatusMode,
		holeId: Long?,
	) {
		when (mode) {
			SetStatusMode.ADD -> postWithCode(url("$baseUrl/user/favorites"), buildJsonObject {
				put("hole_id", holeId)
			})

			SetStatusMode.DELETE -> deleteWithCode(url("$baseUrl/user/favorites"), buildJsonObject {
				put("hole_id", holeId)
			})
		}
	}

	fun setSubscription(
		mode: SetStatusMode,
		holeId: Long?,
	) {
		when (mode) {
			SetStatusMode.ADD -> postWithCode(url("$baseUrl/users/subscriptions"), buildJsonObject {
				put("hole_id", holeId)
			})

			SetStatusMode.DELETE -> deleteWithCode(url("$baseUrl/users/subscriptions"), buildJsonObject {
				put("hole_id", holeId)
			})
		}
	}

	/// Modify a floor
	fun modifyFloor(
		content: String,
		floorId: Long?,
	): Int {
		return patchWithCode(url("$baseUrl/floors/$floorId/_webvpn"), buildJsonObject {
			put("content", content)
		})
	}

	/// Delete a floor
	fun deleteFloor(floorId: Long?): Int {
		return deleteWithCode(url("$baseUrl/floors/$floorId"), JsonObject(emptyMap()))
	}

	/// Get user's punishment history
	fun getPunishmentHistory(): List<OtPunishment> {
		return get(url("$baseUrl/users/me/punishments"))
	}

	/// Get user silence status by floor ID
	/// Returns a map where keys are division IDs and values silence end times
	fun adminGetUserSilenceByFloorId(floorId: Long): Map<String, String> {
		return get(url("$baseUrl/floors/$floorId/user_silence"))
	}

	/// Admin API below
	fun adminGetReports(
		startReport: Long,
		length: Long = 10,
	): List<OtReport> {
		return get(url("$baseUrl/reports") {
			add("offset", startReport.toString())
			add("size", length.toString())
		})
	}

	fun adminGetAuditFloors(
		startTime: OffsetDateTime,
		open: Boolean,
		length: Long = 10,
	): List<OtAudit> {
		return get(url("$baseUrl/floors/_sensitive") {
			add("offset", startTime.toForumString())
			add("size", length.toString())
			add("all", false.toString())
			add("open", open.toString())
			add("order_by", "time_created")
		})
	}

	fun adminSetAuditFloor(
		floorId: Long,
		isActualSensitive: Boolean,
	): Int {
		return patchWithCode(url("$baseUrl/floors/$floorId/_sensitive/_webvpn"), buildJsonObject {
			put("is_actual_sensitive", isActualSensitive)
		})
	}

	fun adminDeleteFloor(
		floorId: Long?,
		deleteReason: String?,
	): Int {
		return deleteWithCode(url("$baseUrl/floors/$floorId"), buildJsonObject {
			deleteReason?.let { put("delete_reason", it) }
		})
	}

	fun adminForceDeleteHole(holeId: Long): Int {
		return deleteWithCode(url("$baseUrl/holes/$holeId/_force"), JsonObject(emptyMap()))
	}

	fun getHistory(floorId: Long?): List<OtHistory> {
		return get(url("$baseUrl/floors/$floorId/history"))
	}

	fun adminDeleteHole(holeId: Long?): Int {
		return deleteWithCode(url("$baseUrl/holes/$holeId"), JsonObject(emptyMap()))
	}

	fun adminLockHole(
		holeId: Long?,
		lock: Boolean,
	): Int {
		return patchWithCode(url("$baseUrl/holes/$holeId/_webvpn"), buildJsonObject {
			put("lock", lock)
		})
	}

	fun adminUndeleteHole(holeId: Long?): Int {
		return patchWithCode(url("$baseUrl/holes/$holeId/_webvpn"), buildJsonObject {
			put("unhidden", true)
		})
	}

	@Deprecated(
		"Use adminAddPenaltyDays instead",
		ReplaceWith("adminAddPenaltyDays(floorId, penaltyDays)"),
	)
	fun adminAddPenalty(
		floorId: Long?,
		penaltyLevel: Long,
	): Int {
		return postWithCode(url("$baseUrl/penalty/$floorId"), buildJsonObject {
			put("penalty_level", penaltyLevel)
		})
	}

	fun adminAddPenaltyDays(
		floorId: Long?,
		penaltyDays: Long,
	): Int {
		return postWithCode(url("$baseUrl/penalty/$floorId"), buildJsonObject {
			put("days", penaltyDays)
		})
	}

	fun adminModifyDivision(
		id: Long,
		name: String?,
		description: String?,
		pinned: List<Long>?,
	): Int {
		return patchWithCode(url("$baseUrl/divisions/$id/_webvpn"), buildJsonObject {
			name?.let { put("name", it) }
			description?.let { put("description", it) }
			pinned?.let { put("pinned", dxrJson.encodeToJsonElement(it)) }
		})
	}

	fun adminAddSpecialTag(
		tag: String,
		floorId: Long?,
	): Int {
		return patchWithCode(url("$baseUrl/floors/$floorId/_webvpn"), buildJsonObject {
			put("special_tag", tag)
		})
	}

	fun adminUpdateTagAndDivision(
		tag: List<OtTag>,
		holeId: Long?,
		divisionId: Long?,
	): Int {
		return patchWithCode(url("$baseUrl/holes/$holeId/_webvpn"), buildJsonObject {
			put("tags", dxrJson.encodeToJsonElement(tag))
			put("division_id", divisionId)
		})
	}

	fun adminFoldFloor(
		fold: List<String>,
		floorId: Long?,
	): Int {
		return patchWithCode(url("$baseUrl/floors/$floorId/_webvpn"), buildJsonObject {
			put("fold", dxrJson.encodeToJsonElement(fold))
		})
	}

	fun adminChangePassword(
		email: String,
		password: String,
	): Int {
		return patchWithCode(url("$baseUrl/register"), buildJsonObject {
			put("email", email)
			put("password", password)
		})
	}

	fun adminSendMessage(
		message: String,
		ids: List<Long>,
	): Int {
		return postWithCode(url("$baseUrl/messages"), buildJsonObject {
			put("content", message)
			put("recipients", dxrJson.encodeToJsonElement(ids))
		})
	}

	fun adminSetReportDealt(reportId: Long): Int {
		return deleteWithCode(url("$baseUrl/reports/$reportId"), JsonObject(emptyMap()))
	}

	fun adminGetPunishmentHistory(floorId: Long): List<String> {
		return get(url("$baseUrl/floors/$floorId/punishment"))
	}

	/// Upload or update Push Notification token to server
	fun updatePushNotificationToken(
		token: String,
		id: String,
		service: PushNotificationServiceType,
	) {
		patchWithCode(url("$baseUrl/users/push-tokens/_webvpn"), buildJsonObject {
			put("service", service.toStringRepresentation())
			put("device_id", id)
			put("token", token)
		})
	}

	fun deletePushNotificationToken(deviceId: String) {
		deleteWithCode(url("$baseUrl/users/push-tokens"), buildJsonObject {
			put("device_id", deviceId)
		})
	}

	fun deleteAllPushNotificationToken(): Int {
		return deleteWithCode(url("$baseUrl/users/push-tokens/_all"), JsonObject(emptyMap()))
	}

	fun getPostRegisterQuestions(): Pair<List<QuizQuestion>?, Int> {
		return get<JsonObject>(url("$baseAuthUrl/register/questions")).run {
			this["questions"]?.let { dxrJson.decodeFromJsonElement<List<QuizQuestion>>(it) } to
				checkNotNull(this["version"]).let { dxrJson.decodeFromJsonElement(it) }
		}
	}

	fun submitAnswers(
		answers: List<QuizAnswer>,
		version: Long,
	): List<Long>? {
		return post<JsonObject>(url("$baseAuthUrl/register/questions/_answer"), buildJsonObject {
			put("answers", dxrJson.encodeToJsonElement(answers))
			put("version", version)
		}).run {
			if (this["correct"]?.let { dxrJson.decodeFromJsonElement<Boolean>(it) } == true) {
				listOf()
			} else {
				this["wrong_question_ids"]?.let { dxrJson.decodeFromJsonElement<List<Long>>(it) }
			}
		}
	}
}
