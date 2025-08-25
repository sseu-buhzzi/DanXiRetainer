package com.buhzzi.danxiretainer.model.settings

import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.util.JavaScriptExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DxrRetentionDecider(
	val decideJavaScript: String,
) {
	suspend fun tryRetain(request: DxrRetentionRequest) = withContext(Dispatchers.IO) {
		val decideResult = JavaScriptExecutor.execute(
			"(request => JSON.stringify(Boolean($decideJavaScript)))(${request.jsonString});",
		)
		if (decideResult == "true") {
			DxrRetention.writeRetainedJson(request.path, request.retention)
			true
		} else {
			false
		}
	}
}
