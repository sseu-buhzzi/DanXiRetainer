package com.buhzzi.danxiretainer.util

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

fun parseJwt(jwtString: String) = jwtString.split('.')
	.apply { check(size == 3) { "JWT must have 3 parts" } }[1]
	.toBytesBase64(Base64.NO_WRAP or Base64.URL_SAFE).toStringUtf8()
	.let { Json.parseToJsonElement(it) }

fun getJwtExpiration(jwtString: String) = runCatching {
	val exp = parseJwt(jwtString)
		.jsonObject["exp"]
		.let { requireNotNull(it) { jwtString } }
		.jsonPrimitive
		.long
	Instant.ofEpochSecond(exp)
		.atZone(ZoneId.systemDefault())
		.toOffsetDateTime()
}.getOrNull()

fun judgeJwtValid(jwtString: String) = getJwtExpiration(jwtString)?.isAfter(OffsetDateTime.now()) == true
