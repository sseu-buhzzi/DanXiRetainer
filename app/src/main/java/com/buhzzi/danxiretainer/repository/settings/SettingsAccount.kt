package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Prefs
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.model.forum.OtUser
import kotlinx.coroutines.flow.map

val Keys.email get() = stringPreferencesKey("email")
var Prefs.email by ItemDelegate(Keys.email)
val Prefs.emailFlow get() = getFlow(Keys.email)


val Keys.passwordCt get() = stringPreferencesKey("password_ct")
var Prefs.passwordCt by ItemDelegate(Keys.passwordCt)
val Prefs.passwordCtFlow get() = getFlow(Keys.passwordCt)


val Keys.userProfileJsonString get() = stringPreferencesKey("user_profile_json_string")
var Prefs.userProfileJsonString by ItemDelegate(Keys.userProfileJsonString)
val Prefs.userProfileJsonStringFlow get() = getFlow(Keys.userProfileJsonString)
var Models.userProfile
	get() = decodeUserProfileJsonString(Prefs.userProfileJsonString)
	set(value) {
		Prefs.userProfileJsonString = encodeUserProfileJsonString(value)
	}
val Models.userProfileFlow
	get() = Prefs.userProfileJsonStringFlow.map { decodeUserProfileJsonString(it) }
val Models.userProfileNotNull
	get() = checkNotNull(Models.userProfile) { "DxrSettings.Models.userProfileNotNull" }

private fun encodeUserProfileJsonString(userProfile: OtUser?) =
	encodeModelJsonString(userProfile)

private fun decodeUserProfileJsonString(string: String?) =
	decodeModelJsonString<OtUser>(string)


val Keys.accessJwt get() = stringPreferencesKey("access_jwt")
var Prefs.accessJwt by ItemDelegate(Keys.accessJwt)
val Prefs.accessJwtFlow get() = getFlow(Keys.accessJwt)


val Keys.refreshJwt get() = stringPreferencesKey("refresh_jwt")
var Prefs.refreshJwt by ItemDelegate(Keys.refreshJwt)
val Prefs.refreshJwtFlow get() = getFlow(Keys.refreshJwt)


val Keys.shouldLoadUserAfterJwt get() = booleanPreferencesKey("should_load_user_after_jwt")
var Prefs.shouldLoadUserAfterJwt by ItemDelegate(Keys.shouldLoadUserAfterJwt)
val Prefs.shouldLoadUserAfterJwtFlow get() = getFlow(Keys.shouldLoadUserAfterJwt)
val Models.shouldLoadUserAfterJwtOrDefault
	get() = Prefs.shouldLoadUserAfterJwt != false
val Models.shouldLoadUserAfterJwtOrDefaultFlow
	get() = Prefs.shouldLoadUserAfterJwtFlow.map { it != false }
