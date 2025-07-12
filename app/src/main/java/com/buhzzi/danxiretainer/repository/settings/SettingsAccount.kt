package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Items
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelJsonString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.model.forum.OtUser
import kotlinx.coroutines.flow.map

val Keys.email get() = stringPreferencesKey("email")
var Items.email by ItemDelegate(Keys.email)
val Items.emailFlow get() = getFlow(Keys.email)


val Keys.passwordCt get() = stringPreferencesKey("password_ct")
var Items.passwordCt by ItemDelegate(Keys.passwordCt)
val Items.passwordCtFlow get() = getFlow(Keys.passwordCt)


val Keys.userProfileJsonString get() = stringPreferencesKey("user_profile_json_string")
var Items.userProfileJsonString by ItemDelegate(Keys.userProfileJsonString)
val Items.userProfileJsonStringFlow get() = getFlow(Keys.userProfileJsonString)
var Models.userProfile
	get() = decodeUserProfileJsonString(Items.userProfileJsonString)
	set(value) {
		Items.userProfileJsonString = encodeUserProfileJsonString(value)
	}
val Models.userProfileFlow
	get() = Items.userProfileJsonStringFlow.map { decodeUserProfileJsonString(it) }
val Models.userProfileNotNull
	get() = checkNotNull(Models.userProfile) { "DxrSettings.Models.userProfileNotNull" }

private fun encodeUserProfileJsonString(userProfile: OtUser?) =
	encodeModelJsonString(userProfile)

private fun decodeUserProfileJsonString(string: String?) =
	decodeModelJsonString<OtUser>(string)


val Keys.accessJwt get() = stringPreferencesKey("access_jwt")
var Items.accessJwt by ItemDelegate(Keys.accessJwt)
val Items.accessJwtFlow get() = getFlow(Keys.accessJwt)


val Keys.refreshJwt get() = stringPreferencesKey("refresh_jwt")
var Items.refreshJwt by ItemDelegate(Keys.refreshJwt)
val Items.refreshJwtFlow get() = getFlow(Keys.refreshJwt)


val Keys.shouldLoadUserAfterJwt get() = booleanPreferencesKey("should_load_user_after_jwt")
var Items.shouldLoadUserAfterJwt by ItemDelegate(Keys.shouldLoadUserAfterJwt)
val Items.shouldLoadUserAfterJwtFlow get() = getFlow(Keys.shouldLoadUserAfterJwt)
val Models.shouldLoadUserAfterJwtOrDefault
	get() = Items.shouldLoadUserAfterJwt != false
val Models.shouldLoadUserAfterJwtOrDefaultFlow
	get() = Items.shouldLoadUserAfterJwtFlow.map { it != false }
