package com.buhzzi.danxiretainer.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.DxrDestination

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPageContent(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState()),
	) {
		NavigateListItem(
			DxrDestination.SettingsAccount,
			stringResource(R.string.account_label),
			Icons.Default.AccountBox,
		)

		NavigateListItem(
			DxrDestination.SettingsNetwork,
			stringResource(R.string.network_label),
			Icons.Default.Dns,
		)

		NavigateListItem(
			DxrDestination.SettingsGeneral,
			stringResource(R.string.general_label),
			Icons.Default.MoreHoriz,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageTopBar() {
	TopAppBar({
		Text(stringResource(R.string.settings_label))
	})
}
