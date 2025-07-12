package com.buhzzi.danxiretainer.page.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.LocalNavController
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.dxrPrettyJson
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsAccountUserProfilePage() {
	DxrScaffoldWrapper(
		topBar = {
			SettingsSubpageTopBar("${stringResource(R.string.settings_label)} - ${stringResource(R.string.account_label)} - ${stringResource(R.string.user_profile_label)}")
		}
	) { contentPadding ->
		val navController = LocalNavController.current

		Column(
			modifier = Modifier
				.padding(contentPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
		) {
			val userProfileJsonNullable by DxrSettings.Models.userProfileFlow.map { userProfile ->
				dxrJson.encodeToJsonElement(userProfile) as? JsonObject
			}.collectAsState(null)
			val userProfileJson = userProfileJsonNullable ?: run {
				ListItem(
					{
						Text(stringResource(R.string.no_user_profile_label))
					},
					modifier = Modifier
						.clickable {
							navController.popBackStack()
						},
					leadingContent = {
						Icon(Icons.Default.Close, null)
					},
				)
				return@Column
			}

			userProfileJson.forEach { (key, value) ->
				ListItem(
					{
						Text(key)
					},
					supportingContent = {
						Surface(
							modifier = Modifier
								.fillMaxWidth()
								.clickable {
									// TODO 複製値
								},
							shape = RoundedCornerShape(4.dp),
							color = MaterialTheme.colorScheme.surfaceVariant,
							contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
						) {
							Text(
								dxrPrettyJson.encodeToString(value),
								modifier = Modifier
									.padding(4.dp),
								style = MaterialTheme.typography.bodySmall.copy(
									fontFamily = FontFamily.Monospace,
								),
							)
						}
					},
					leadingContent = {
						Icon(Icons.AutoMirrored.Default.List, null)
					},
				)
			}

			HorizontalDivider()

			ListItem(
				{
					Text(stringResource(R.string.erase_user_profile_label))
				},
				modifier = Modifier
					.clickable { DxrSettings.Models.userProfile = null },
				leadingContent = {
					Icon(Icons.Default.DeleteForever, null)
				}
			)
		}
	}
}
