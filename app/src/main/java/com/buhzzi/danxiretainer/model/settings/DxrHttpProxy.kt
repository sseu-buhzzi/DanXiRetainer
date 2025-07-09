package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrHttpProxy(
	val enabled: Boolean? = null,
	val host: String? = null,
	val port: Int? = null,
) {
	val enabledNotNull get() = checkNotNull(enabled) { this }
	val hostNotNull get() = checkNotNull(host) { this }
	val portNotNull get() = checkNotNull(port) { this }
}
