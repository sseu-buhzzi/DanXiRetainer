package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrHttpProxy
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Items
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import com.buhzzi.danxiretainer.util.dxrJson
import kotlinx.coroutines.flow.map

val Keys.authBaseUrl get() = stringPreferencesKey("auth_base_url")
var Items.authBaseUrl by ItemDelegate(Keys.authBaseUrl)
val Items.authBaseUrlFlow get() = getFlow(Keys.authBaseUrl)

val Keys.forumBaseUrl get() = stringPreferencesKey("forum_base_url")
var Items.forumBaseUrl by ItemDelegate(Keys.forumBaseUrl)
val Items.forumBaseUrlFlow get() = getFlow(Keys.forumBaseUrl)

val Keys.imageBaseUrl get() = stringPreferencesKey("image_base_url")
var Items.imageBaseUrl by ItemDelegate(Keys.imageBaseUrl)
val Items.imageBaseUrlFlow get() = getFlow(Keys.imageBaseUrl)

val Keys.httpProxyJsonString get() = stringPreferencesKey("http_proxy_json_string")
var Items.httpProxyJsonString by ItemDelegate(Keys.httpProxyJsonString)
val Items.httpProxyJsonStringFlow get() = getFlow(Keys.httpProxyJsonString)
var Models.httpProxy
	get() = decodeHttpProxyJsonString(Items.httpProxyJsonString)
	set(value) {
		Items.httpProxyJsonString = encodeHttpProxyJsonString(value)
	}
val Models.httpProxyFlow
	get() = Items.httpProxyJsonStringFlow.map {
		decodeHttpProxyJsonString(it)
	}

private fun encodeHttpProxyJsonString(httpProxy: DxrHttpProxy?) = httpProxy?.let {
	dxrJson.encodeToString(it)
}

private fun decodeHttpProxyJsonString(string: String?) = string?.runCatching {
	dxrJson.decodeFromString<DxrHttpProxy>(this)
}?.getOrNull()
