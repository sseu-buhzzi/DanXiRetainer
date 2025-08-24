package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrHttpProxy
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Prefs
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.common.Constant
import kotlinx.coroutines.flow.map

@Suppress("UnusedReceiverParameter")
val Keys.authBaseUrl get() = stringPreferencesKey("auth_base_url")
var Prefs.authBaseUrl by ItemDelegate(Keys.authBaseUrl)
@Suppress("UnusedReceiverParameter")
val Prefs.authBaseUrlFlow get() = getFlow(Keys.authBaseUrl)
@Suppress("UnusedReceiverParameter")
val Models.authBaseUrlOrDefault
	get() = Prefs.authBaseUrl ?: Constant.AUTH_BASE_URL
@Suppress("UnusedReceiverParameter")
val Models.authBaseUrlOrDefaultFlow
	get() = Prefs.authBaseUrlFlow.map { it ?: Constant.AUTH_BASE_URL }


@Suppress("UnusedReceiverParameter")
val Keys.forumBaseUrl get() = stringPreferencesKey("forum_base_url")
var Prefs.forumBaseUrl by ItemDelegate(Keys.forumBaseUrl)
@Suppress("UnusedReceiverParameter")
val Prefs.forumBaseUrlFlow get() = getFlow(Keys.forumBaseUrl)
@Suppress("UnusedReceiverParameter")
val Models.forumBaseUrlOrDefault
	get() = Prefs.forumBaseUrl ?: Constant.FORUM_BASE_URL
@Suppress("UnusedReceiverParameter")
val Models.forumBaseUrlOrDefaultFlow
	get() = Prefs.forumBaseUrlFlow.map { it ?: Constant.FORUM_BASE_URL }


@Suppress("UnusedReceiverParameter")
val Keys.imageBaseUrl get() = stringPreferencesKey("image_base_url")
var Prefs.imageBaseUrl by ItemDelegate(Keys.imageBaseUrl)
@Suppress("UnusedReceiverParameter")
val Prefs.imageBaseUrlFlow get() = getFlow(Keys.imageBaseUrl)
@Suppress("UnusedReceiverParameter")
val Models.imageBaseUrlOrDefault
	get() = Prefs.imageBaseUrl ?: Constant.IMAGE_BASE_URL
@Suppress("UnusedReceiverParameter")
val Models.imageBaseUrlOrDefaultFlow
	get() = Prefs.imageBaseUrlFlow.map { it ?: Constant.IMAGE_BASE_URL }


@Suppress("UnusedReceiverParameter")
val Keys.httpProxyJsonString get() = stringPreferencesKey("http_proxy_json_string")
var Prefs.httpProxyJsonString by ItemDelegate(Keys.httpProxyJsonString)
@Suppress("UnusedReceiverParameter")
val Prefs.httpProxyJsonStringFlow get() = getFlow(Keys.httpProxyJsonString)
@Suppress("UnusedReceiverParameter")
var Models.httpProxy
	get() = decodeHttpProxyJsonString(Prefs.httpProxyJsonString)
	set(value) {
		Prefs.httpProxyJsonString = encodeHttpProxyJsonString(value)
	}
@Suppress("UnusedReceiverParameter")
val Models.httpProxyFlow
	get() = Prefs.httpProxyJsonStringFlow.map { decodeHttpProxyJsonString(it) }

private fun encodeHttpProxyJsonString(httpProxy: DxrHttpProxy?) =
	encodeModelJsonString(httpProxy)

private fun decodeHttpProxyJsonString(string: String?) =
	decodeModelJsonString<DxrHttpProxy>(string)
