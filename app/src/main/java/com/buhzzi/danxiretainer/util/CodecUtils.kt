package com.buhzzi.danxiretainer.util

import android.util.Base64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun OffsetDateTime.toStringRfc3339(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

fun String.toDateTimeRfc3339(): OffsetDateTime = OffsetDateTime.parse(this)

fun ByteArray.toStringUtf8() = String(this, Charsets.UTF_8)

fun String.toBytesUtf8() = toByteArray(Charsets.UTF_8)

fun ByteArray.toStringBase64(flags: Int = Base64.NO_WRAP): String = Base64.encodeToString(this, flags)

fun String.toBytesBase64(flags: Int = Base64.NO_WRAP): ByteArray = Base64.decode(this, flags)

fun String.escapeTsv() = buildString(length) {
	this@escapeTsv.forEach { char ->
		append(when (char) {
			'\t' -> "\\t"
			'\n' -> "\\n"
			'\r' -> "\\r"
			'\\' -> "\\\\"
			else -> char
		})
	}
}

fun String.unescapeTsv() = buildString(length) {
	var i = 0
	while (i < this@unescapeTsv.length) {
		val char = this@unescapeTsv[i++]
		if (char != '\\') {
			append(char)
			continue
		}
		if (i >= this@unescapeTsv.length) {
			append('\\')
			break
		}
		when (val nextChar = this@unescapeTsv[i++]) {
			't' -> append('\t')
			'n' -> append('\n')
			'r' -> append('\r')
			'\\' -> append('\\')
			else -> {
				append('\\')
				append(nextChar)
			}
		}
	}
}

@OptIn(ExperimentalSerializationApi::class)
val dxrJson = Json {
	encodeDefaults = true
	isLenient = false
	allowSpecialFloatingPointValues = true
	allowStructuredMapKeys = true
	ignoreUnknownKeys = true
	namingStrategy = JsonNamingStrategy.SnakeCase
}

@OptIn(ExperimentalSerializationApi::class)
val dxrPrettyJson = Json(dxrJson) {
	prettyPrint = true
	prettyPrintIndent = "\t"
}
