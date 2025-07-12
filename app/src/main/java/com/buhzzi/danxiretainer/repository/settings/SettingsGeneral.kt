package com.buhzzi.danxiretainer.repository.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.ItemDelegate
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Items
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Keys
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.Models
import com.buhzzi.danxiretainer.repository.settings.DxrSettings.getFlow
import dart.package0.dan_xi.provider.SortOrder
import kotlinx.coroutines.flow.map

val Keys.cleanMode get() = booleanPreferencesKey("clean_mode")
var Items.cleanMode by ItemDelegate(Keys.cleanMode)
val Items.cleanModeFlow get() = getFlow(Keys.cleanMode)

val Keys.backgroundImagePathString get() = stringPreferencesKey("background_image_path_string")
var Items.backgroundImagePathString by ItemDelegate(Keys.backgroundImagePathString)
val Items.backgroundImagePathStringFlow get() = getFlow(Keys.backgroundImagePathString)

// TODO 預設FORUM_API
val Keys.contentSourceString get() = stringPreferencesKey("content_source")
var Items.contentSourceString by ItemDelegate(Keys.contentSourceString)
val Items.contentSourceStringFlow get() = getFlow(Keys.contentSourceString)
var Models.contentSource
	get() = decodeContentSourceString(Items.contentSourceString)
	set(value) {
		Items.contentSourceString = encodeContentSourceString(value)
	}
val Models.contentSourceFlow
	get() = Items.contentSourceStringFlow.map {
		decodeContentSourceString(it)
	}

private fun decodeContentSourceString(string: String?) = string?.let {
	DxrContentSource.valueOf(it)
}

private fun encodeContentSourceString(contentResource: DxrContentSource?) =
	contentResource?.name

val Keys.sortOrderString get() = stringPreferencesKey("sort_order_string")
var Items.sortOrderString by ItemDelegate(Keys.sortOrderString)
val Items.sortOrderStringFlow get() = getFlow(Keys.sortOrderString)
var Models.sortOrder
	get() = decodeSortOrderString(Items.sortOrderString)
	set(value) {
		Items.sortOrderString = encodeSortOrderString(value)
	}
val Models.sortOrderFlow
	get() = Items.sortOrderStringFlow.map {
		decodeSortOrderString(it)
	}

private fun decodeSortOrderString(string: String?) = string?.let {
	SortOrder.valueOf(it)
}

private fun encodeSortOrderString(contentResource: SortOrder?) =
	contentResource?.name


val Keys.pagerScrollOrientationString get() = stringPreferencesKey("pager_scroll_orientation_string")
var Items.pagerScrollOrientationString by ItemDelegate(Keys.pagerScrollOrientationString)
val Items.pagerScrollOrientationStringFlow get() = getFlow(Keys.pagerScrollOrientationString)
var Models.pagerScrollOrientation
	get() = decodePagerScrollOrientationString(Items.pagerScrollOrientationString)
	set(value) {
		Items.pagerScrollOrientationString = encodePagerScrollOrientationString(value)
	}
val Models.pagerScrollOrientationFlow
	get() = Items.pagerScrollOrientationStringFlow.map {
		decodePagerScrollOrientationString(it)
	}

private fun decodePagerScrollOrientationString(string: String?) = string?.let {
	DxrPagerScrollOrientation.valueOf(it)
}

private fun encodePagerScrollOrientationString(contentResource: DxrPagerScrollOrientation?) =
	contentResource?.name

val Keys.floorsReversed get() = booleanPreferencesKey("floors_reversed")
var Items.floorsReversed by ItemDelegate(Keys.floorsReversed)
val Items.floorsReversedFlow get() = getFlow(Keys.floorsReversed)
