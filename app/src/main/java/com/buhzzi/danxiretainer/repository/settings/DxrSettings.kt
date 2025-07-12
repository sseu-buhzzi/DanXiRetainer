package com.buhzzi.danxiretainer.repository.settings

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.buhzzi.danxiretainer.util.dxrJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object DxrSettings {
	private const val SETTINGS_DATA_STORE_NAME = "settings"

	private val Application.dataStore by preferencesDataStore(SETTINGS_DATA_STORE_NAME)

	private lateinit var app: Application

	@Volatile
	private var preferences: MutablePreferences? = null

	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	fun init(context: Context) {
		app = context.applicationContext as Application
		scope.launch {
			app.dataStore.data.collect { newPreferences ->
				preferences = newPreferences.toMutablePreferences()
			}
		}
	}

	fun <T> getFlow(key: Preferences.Key<T>) = app.dataStore.data.map { preferences ->
		preferences[key]
	}

	private operator fun <T> get(key: Preferences.Key<T>) = preferences?.get(key)

	private operator fun <T> set(key: Preferences.Key<T>, value: T?) {
		if (value == null) {
			preferences?.remove(key)
		} else {
			preferences?.set(key, value)
		}
		scope.launch {
			app.dataStore.edit { preferences ->
				if (value == null) {
					preferences.remove(key)
				} else {
					preferences[key] = value
				}
			}
		}
	}

	class ItemDelegate<T>(
		private val key: Preferences.Key<T>,
	) : ReadWriteProperty<Any, T?> {
		override fun getValue(thisRef: Any, property: KProperty<*>) = get(key)

		override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) = set(key, value)
	}

	inline fun <reified T : Enum<T>> decodeModelEnumString(string: String?) =
		enumValues<T>().firstOrNull { it.name == string }

	inline fun <reified T : Enum<T>> encodeModelEnumString(model: T?) = model?.name

	inline fun <reified T> decodeModelJsonString(string: String?) = string?.runCatching {
		dxrJson.decodeFromString<T>(this)
	}?.getOrNull()

	inline fun <reified T : Any> encodeModelJsonString(model: T?) = model?.let {
		dxrJson.encodeToString(it)
	}

	object Keys
	object Items
	object Models
}
