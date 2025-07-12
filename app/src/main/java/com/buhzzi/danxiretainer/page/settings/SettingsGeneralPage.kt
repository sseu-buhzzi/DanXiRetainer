package com.buhzzi.danxiretainer.page.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.model.settings.DxrPagerScrollOrientation
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.cleanMode
import com.buhzzi.danxiretainer.repository.settings.cleanModeFlow
import com.buhzzi.danxiretainer.repository.settings.contentSource
import com.buhzzi.danxiretainer.repository.settings.contentSourceFlow
import com.buhzzi.danxiretainer.repository.settings.floorsReversed
import com.buhzzi.danxiretainer.repository.settings.floorsReversedFlow
import com.buhzzi.danxiretainer.repository.settings.pagerScrollOrientation
import com.buhzzi.danxiretainer.repository.settings.pagerScrollOrientationFlow
import com.buhzzi.danxiretainer.repository.settings.sortOrder
import com.buhzzi.danxiretainer.repository.settings.sortOrderFlow
import dart.package0.dan_xi.provider.SortOrder

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsGeneralPage() {
	DxrScaffoldWrapper(
		topBar = {
			SettingsSubpageTopBar("${stringResource(R.string.settings_label)} - ${stringResource(R.string.general_label)}")
		}
	) { contentPadding ->
		Column(
			modifier = Modifier
				.padding(contentPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			val cleanMode by DxrSettings.Items.cleanModeFlow.collectAsState(null)
			ToggleListItem(
				cleanMode == true,
				stringResource(R.string.clean_mode_label),
				stringResource(R.string.clean_mode_description),
				Icons.Default.AcUnit,
			) { checked ->
				DxrSettings.Items.cleanMode = checked
			}

			SelectBackgroundImageListItem()

			val contentSource by DxrSettings.Models.contentSourceFlow.collectAsState(null)
			val contentSources = listOf(
				"Forum API",
				"Retention",
			)
			val contentSourceActions = listOf(
				{
					DxrSettings.Models.contentSource = DxrContentSource.FORUM_API
					true
				},
				{
					DxrSettings.Models.contentSource = DxrContentSource.RETENTION
					true
				},
			)
			SingleSelectListItem(
				contentSources,
				contentSource?.ordinal ?: 0,
				stringResource(R.string.content_sources_label),
				settingsValueStringResource(
					contentSource?.ordinal?.let { contentSources[it] },
				),
				Icons.Default.Source,
			) { selection ->
				selection?.let { contentSourceActions[it]() } == true
			}

			val sortOrder by DxrSettings.Models.sortOrderFlow.collectAsState(null)
			val sortOrders = listOf(
				"Last Replied",
				"Last Created",
			)
			val sortOrderActions = listOf(
				{
					DxrSettings.Models.sortOrder = SortOrder.LAST_REPLIED
					true
				},
				{
					DxrSettings.Models.sortOrder = SortOrder.LAST_CREATED
					true
				},
			)
			SingleSelectListItem(
				sortOrders,
				sortOrder?.ordinal ?: 0,
				stringResource(R.string.sort_order_label),
				settingsValueStringResource(
					sortOrder?.ordinal?.let { sortOrders[it] },
				),
				Icons.AutoMirrored.Default.Sort,
			) { selection ->
				selection?.let { sortOrderActions[it]() } == true
			}

			val pagerScrollOrientation by DxrSettings.Models.pagerScrollOrientationFlow.collectAsState(null)
			// TODO put it in enum class members
			val pagerScrollOrientations = listOf(
				"Horizontal",
				"Vertical",
			)
			ListItem(
				{
					Text(stringResource(R.string.pager_scroll_orientation_label))
				},
				modifier = Modifier
					.clickable {
						DxrSettings.Models.pagerScrollOrientation = if (pagerScrollOrientation != DxrPagerScrollOrientation.HORIZONTAL) {
							DxrPagerScrollOrientation.HORIZONTAL
						} else {
							DxrPagerScrollOrientation.VERTICAL
						}
					},
				supportingContent = {
					Text(
						settingsValueStringResource(
							pagerScrollOrientation?.ordinal?.let { pagerScrollOrientations[it] },
						),
					)
				},
				leadingContent = {
					Icon(
						when (pagerScrollOrientation) {
							DxrPagerScrollOrientation.HORIZONTAL -> Icons.Default.Swipe
							DxrPagerScrollOrientation.VERTICAL -> Icons.Default.SwipeVertical
							else -> Icons.Default.QuestionMark
						}, null
					)
				},
			)

			val floorsReversed by DxrSettings.Items.floorsReversedFlow.collectAsState(null)
			ToggleListItem(
				floorsReversed == true,
				stringResource(R.string.floors_reversed_label),
				stringResource(R.string.floors_reversed_description),
				Icons.Default.VerticalAlignBottom,
			) { checked ->
				DxrSettings.Items.floorsReversed = checked
			}
		}
	}
}
