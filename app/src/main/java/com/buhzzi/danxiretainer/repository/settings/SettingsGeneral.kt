package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Items
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.decodeModelEnumString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.encodeModelEnumString
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.coroutines.flow.map
import kotlin.io.path.Path
import kotlin.io.path.pathString

val Keys.cleanMode get() = booleanPreferencesKey("clean_mode")
var Items.cleanMode by ItemDelegate(Keys.cleanMode)
val Items.cleanModeFlow get() = getFlow(Keys.cleanMode)
val Models.cleanModeOrDefault
	get() = Items.cleanMode == true
val Models.cleanModeOrDefaultFlow
	get() = Items.cleanModeFlow.map { it == true }


val Keys.backgroundImagePathString get() = stringPreferencesKey("background_image_path_string")
var Items.backgroundImagePathString by ItemDelegate(Keys.backgroundImagePathString)
val Items.backgroundImagePathStringFlow get() = getFlow(Keys.backgroundImagePathString)
var Models.backgroundImagePath
	get() = Items.backgroundImagePathString?.let { Path(it) }
	set(value) {
		Items.backgroundImagePathString = value?.pathString
	}
val Models.backgroundImagePathFlow
	get() = Items.backgroundImagePathStringFlow.map { it?.let { Path(it) } }


val Keys.contentSourceString get() = stringPreferencesKey("content_source")
var Items.contentSourceString by ItemDelegate(Keys.contentSourceString)
val Items.contentSourceStringFlow get() = getFlow(Keys.contentSourceString)
var Models.contentSource
	get() = decodeContentSourceString(Items.contentSourceString)
	set(value) {
		Items.contentSourceString = encodeContentSourceString(value)
	}
val Models.contentSourceFlow
	get() = Items.contentSourceStringFlow.map { decodeContentSourceString(it) }
val Models.contentSourceOrDefault
	get() = contentSource ?: DxrContentSource.FORUM_API
val Models.contentSourceOrDefaultFlow
	get() = contentSourceFlow.map { it ?: DxrContentSource.FORUM_API }

private fun decodeContentSourceString(string: String?) =
	decodeModelEnumString<DxrContentSource>(string)

private fun encodeContentSourceString(contentSource: DxrContentSource?) =
	encodeModelEnumString(contentSource)


val Keys.sortOrderString get() = stringPreferencesKey("sort_order_string")
var Items.sortOrderString by ItemDelegate(Keys.sortOrderString)
val Items.sortOrderStringFlow get() = getFlow(Keys.sortOrderString)
var Models.sortOrder
	get() = decodeSortOrderString(Items.sortOrderString)
	set(value) {
		Items.sortOrderString = encodeSortOrderString(value)
	}
val Models.sortOrderFlow
	get() = Items.sortOrderStringFlow.map { decodeSortOrderString(it) }
val Models.sortOrderOrDefault
	get() = sortOrder ?: SortOrder.LAST_REPLIED
val Models.sortOrderOrDefaultFlow
	get() = sortOrderFlow.map { it ?: SortOrder.LAST_REPLIED }

private fun decodeSortOrderString(string: String?) =
	decodeModelEnumString<SortOrder>(string)

private fun encodeSortOrderString(sortOrder: SortOrder?) =
	encodeModelEnumString(sortOrder)


val Keys.pagerScrollOrientationString get() = stringPreferencesKey("pager_scroll_orientation_string")
var Items.pagerScrollOrientationString by ItemDelegate(Keys.pagerScrollOrientationString)
val Items.pagerScrollOrientationStringFlow get() = getFlow(Keys.pagerScrollOrientationString)
var Models.pagerScrollOrientation
	get() = decodePagerScrollOrientationString(Items.pagerScrollOrientationString)
	set(value) {
		Items.pagerScrollOrientationString = encodePagerScrollOrientationString(value)
	}
val Models.pagerScrollOrientationFlow
	get() = Items.pagerScrollOrientationStringFlow.map { decodePagerScrollOrientationString(it) }
val Models.pagerScrollOrientationOrDefault
	get() = pagerScrollOrientation ?: DxrPagerScrollOrientation.VERTICAL
val Models.pagerScrollOrientationOrDefaultFlow
	get() = pagerScrollOrientationFlow.map { it ?: DxrPagerScrollOrientation.VERTICAL }

private fun decodePagerScrollOrientationString(string: String?) =
	decodeModelEnumString<DxrPagerScrollOrientation>(string)

private fun encodePagerScrollOrientationString(pagerScrollOrientation: DxrPagerScrollOrientation?) =
	encodeModelEnumString(pagerScrollOrientation)


val Keys.floorsReversed get() = booleanPreferencesKey("floors_reversed")
var Items.floorsReversed by ItemDelegate(Keys.floorsReversed)
val Items.floorsReversedFlow get() = getFlow(Keys.floorsReversed)
val Models.floorsReversedOrDefault
	get() = Items.floorsReversed == true
val Models.floorsReversedOrDefaultFlow
	get() = Items.floorsReversedFlow.map { it == true }
