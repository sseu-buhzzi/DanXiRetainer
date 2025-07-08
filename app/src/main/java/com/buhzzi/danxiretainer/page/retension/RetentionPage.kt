package com.buhzzi.danxiretainer.page.retension

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.buhzzi.danxiretainer.R

@Composable
fun RetentionPageContent(modifier: Modifier = Modifier) {
		Box(
			modifier = modifier,
		)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RetentionPageTopBar() {
	TopAppBar({
		Text(stringResource(R.string.retention_label))
	})
}
