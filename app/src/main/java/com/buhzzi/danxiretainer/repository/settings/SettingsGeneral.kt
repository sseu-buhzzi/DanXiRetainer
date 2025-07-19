package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.model.settings.DxrRetentionDecider
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Prefs
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelEnumString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelEnumString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.coroutines.flow.map
import kotlin.io.path.Path
import kotlin.io.path.pathString

val Keys.cleanMode get() = booleanPreferencesKey("clean_mode")
var Prefs.cleanMode by ItemDelegate(Keys.cleanMode)
val Prefs.cleanModeFlow get() = getFlow(Keys.cleanMode)
val Models.cleanModeOrDefault
	get() = Prefs.cleanMode == true
val Models.cleanModeOrDefaultFlow
	get() = Prefs.cleanModeFlow.map { it == true }


val Keys.backgroundImagePathString get() = stringPreferencesKey("background_image_path_string")
var Prefs.backgroundImagePathString by ItemDelegate(Keys.backgroundImagePathString)
val Prefs.backgroundImagePathStringFlow get() = getFlow(Keys.backgroundImagePathString)
var Models.backgroundImagePath
	get() = Prefs.backgroundImagePathString?.let { Path(it) }
	set(value) {
		Prefs.backgroundImagePathString = value?.pathString
	}
val Models.backgroundImagePathFlow
	get() = Prefs.backgroundImagePathStringFlow.map { it?.let { Path(it) } }


val Keys.contentSourceString get() = stringPreferencesKey("content_source")
var Prefs.contentSourceString by ItemDelegate(Keys.contentSourceString)
val Prefs.contentSourceStringFlow get() = getFlow(Keys.contentSourceString)
var Models.contentSource
	get() = decodeContentSourceString(Prefs.contentSourceString)
	set(value) {
		Prefs.contentSourceString = encodeContentSourceString(value)
	}
val Models.contentSourceFlow
	get() = Prefs.contentSourceStringFlow.map { decodeContentSourceString(it) }
val Models.contentSourceOrDefault
	get() = contentSource ?: DxrContentSource.FORUM_API
val Models.contentSourceOrDefaultFlow
	get() = contentSourceFlow.map { it ?: DxrContentSource.FORUM_API }

private fun decodeContentSourceString(string: String?) =
	decodeModelEnumString<DxrContentSource>(string)

private fun encodeContentSourceString(contentSource: DxrContentSource?) =
	encodeModelEnumString(contentSource)


val Keys.sortOrderString get() = stringPreferencesKey("sort_order_string")
var Prefs.sortOrderString by ItemDelegate(Keys.sortOrderString)
val Prefs.sortOrderStringFlow get() = getFlow(Keys.sortOrderString)
var Models.sortOrder
	get() = decodeSortOrderString(Prefs.sortOrderString)
	set(value) {
		Prefs.sortOrderString = encodeSortOrderString(value)
	}
val Models.sortOrderFlow
	get() = Prefs.sortOrderStringFlow.map { decodeSortOrderString(it) }
val Models.sortOrderOrDefault
	get() = sortOrder ?: SortOrder.LAST_REPLIED
val Models.sortOrderOrDefaultFlow
	get() = sortOrderFlow.map { it ?: SortOrder.LAST_REPLIED }

private fun decodeSortOrderString(string: String?) =
	decodeModelEnumString<SortOrder>(string)

private fun encodeSortOrderString(sortOrder: SortOrder?) =
	encodeModelEnumString(sortOrder)


val Keys.pagerScrollOrientationString get() = stringPreferencesKey("pager_scroll_orientation_string")
var Prefs.pagerScrollOrientationString by ItemDelegate(Keys.pagerScrollOrientationString)
val Prefs.pagerScrollOrientationStringFlow get() = getFlow(Keys.pagerScrollOrientationString)
var Models.pagerScrollOrientation
	get() = decodePagerScrollOrientationString(Prefs.pagerScrollOrientationString)
	set(value) {
		Prefs.pagerScrollOrientationString = encodePagerScrollOrientationString(value)
	}
val Models.pagerScrollOrientationFlow
	get() = Prefs.pagerScrollOrientationStringFlow.map { decodePagerScrollOrientationString(it) }
val Models.pagerScrollOrientationOrDefault
	get() = pagerScrollOrientation ?: DxrPagerScrollOrientation.VERTICAL
val Models.pagerScrollOrientationOrDefaultFlow
	get() = pagerScrollOrientationFlow.map { it ?: DxrPagerScrollOrientation.VERTICAL }

private fun decodePagerScrollOrientationString(string: String?) =
	decodeModelEnumString<DxrPagerScrollOrientation>(string)

private fun encodePagerScrollOrientationString(pagerScrollOrientation: DxrPagerScrollOrientation?) =
	encodeModelEnumString(pagerScrollOrientation)


val Keys.floorsReversed get() = booleanPreferencesKey("floors_reversed")
var Prefs.floorsReversed by ItemDelegate(Keys.floorsReversed)
val Prefs.floorsReversedFlow get() = getFlow(Keys.floorsReversed)
val Models.floorsReversedOrDefault
	get() = Prefs.floorsReversed == true
val Models.floorsReversedOrDefaultFlow
	get() = Prefs.floorsReversedFlow.map { it == true }


val Keys.retentionDeciderJsString get() = stringPreferencesKey("retention_decider_js_string")
var Prefs.retentionDeciderJsString by ItemDelegate(Keys.retentionDeciderJsString)
val Prefs.retentionDeciderJsStringFlow get() = getFlow(Keys.retentionDeciderJsString)
var Models.retentionDecider
	get() = Prefs.retentionDeciderJsString?.let { DxrRetentionDecider(it) }
	set(value) {
		Prefs.retentionDeciderJsString = value?.decideJavaScript
	}
val Models.retentionDeciderFlow
	get() = Prefs.retentionDeciderJsStringFlow.map { it?.let { DxrRetentionDecider(it) } }
val Models.retentionDeciderOrDefault
	get() = retentionDecider ?: DxrRetentionDecider("true")
val Models.retentionDeciderOrDefaultFlow
	get() = retentionDeciderFlow.map { it ?: DxrRetentionDecider("true") }
