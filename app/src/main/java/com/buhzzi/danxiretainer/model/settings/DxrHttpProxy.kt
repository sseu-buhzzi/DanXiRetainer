package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrHttpProxy(
	val enabled: Boolean? = null,
	val host: String? = null,
	val port: Int? = null,
)
