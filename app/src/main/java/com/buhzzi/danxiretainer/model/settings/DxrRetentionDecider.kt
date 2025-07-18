package com.buhzzi.danxiretainer.model.settings

import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.util.JavaScriptExecutor
import com.buhzzi.danxiretainer.util.dxrJson

class DxrRetentionDecider(
	val decideJavaScript: String,
) {
	fun tryRetain(request: DxrRetentionRequest): Boolean {
		val decideResult = JavaScriptExecutor.execute(
			"""
				const request = ${dxrJson.encodeToString(request.requestJson)};
				Boolean($decideJavaScript);
			""".trimIndent(),
		)
		decideResult == true || return false
		DxrRetention.writeRetainedJson(request.path, request.retention)
		return true
	}
}
