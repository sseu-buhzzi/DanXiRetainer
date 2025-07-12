package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrHttpProxy
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Items
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.common.Constant
import kotlinx.coroutines.flow.map

val Keys.authBaseUrl get() = stringPreferencesKey("auth_base_url")
var Items.authBaseUrl by ItemDelegate(Keys.authBaseUrl)
val Items.authBaseUrlFlow get() = getFlow(Keys.authBaseUrl)
val Models.authBaseUrlOrDefault
	get() = Items.authBaseUrl ?: Constant.AUTH_BASE_URL
val Models.authBaseUrlOrDefaultFlow
	get() = Items.authBaseUrlFlow.map { it ?: Constant.AUTH_BASE_URL }


val Keys.forumBaseUrl get() = stringPreferencesKey("forum_base_url")
var Items.forumBaseUrl by ItemDelegate(Keys.forumBaseUrl)
val Items.forumBaseUrlFlow get() = getFlow(Keys.forumBaseUrl)
val Models.forumBaseUrlOrDefault
	get() = Items.forumBaseUrl ?: Constant.FORUM_BASE_URL
val Models.forumBaseUrlOrDefaultFlow
	get() = Items.forumBaseUrlFlow.map { it ?: Constant.FORUM_BASE_URL }


val Keys.imageBaseUrl get() = stringPreferencesKey("image_base_url")
var Items.imageBaseUrl by ItemDelegate(Keys.imageBaseUrl)
val Items.imageBaseUrlFlow get() = getFlow(Keys.imageBaseUrl)
val Models.imageBaseUrlOrDefault
	get() = Items.imageBaseUrl ?: Constant.IMAGE_BASE_URL
val Models.imageBaseUrlOrDefaultFlow
	get() = Items.imageBaseUrlFlow.map { it ?: Constant.IMAGE_BASE_URL }


val Keys.httpProxyJsonString get() = stringPreferencesKey("http_proxy_json_string")
var Items.httpProxyJsonString by ItemDelegate(Keys.httpProxyJsonString)
val Items.httpProxyJsonStringFlow get() = getFlow(Keys.httpProxyJsonString)
var Models.httpProxy
	get() = decodeHttpProxyJsonString(Items.httpProxyJsonString)
	set(value) {
		Items.httpProxyJsonString = encodeHttpProxyJsonString(value)
	}
val Models.httpProxyFlow
	get() = Items.httpProxyJsonStringFlow.map { decodeHttpProxyJsonString(it) }

private fun encodeHttpProxyJsonString(httpProxy: DxrHttpProxy?) =
	encodeModelJsonString(httpProxy)

private fun decodeHttpProxyJsonString(string: String?) =
	decodeModelJsonString<DxrHttpProxy>(string)
