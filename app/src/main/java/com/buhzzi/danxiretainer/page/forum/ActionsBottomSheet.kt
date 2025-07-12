package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsBottomSheet(
	onDismissRequest: () -> Unit,
	sheetState: SheetState = rememberModalBottomSheetState(),
	content: @Composable ColumnScope.() -> Unit,
) {
	ModalBottomSheet(
		onDismissRequest,
		sheetState = sheetState,
	) {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState()),
			content = content,
		)
	}
}

@Composable
fun ClickCatchingActionBottomSheetItem(
	click: suspend CoroutineScope.() -> Unit,
	content: @Composable () -> Unit,
) {
	val snackbarController = LocalSnackbarController.current

	val scope = rememberCoroutineScope()

	ListItem(
		content,
		modifier = Modifier
			.clickable {
				scope.launch(Dispatchers.IO) {
					runCatchingOnSnackbar(snackbarController) {
						click()
					}
				}
			},
		colors = ListItemDefaults.colors(
			containerColor = Color.Transparent,
		),
	)
}
