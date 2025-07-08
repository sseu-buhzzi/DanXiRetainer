package com.buhzzi.danxiretainer.page.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.DxrDestination
import com.buhzzi.danxiretainer.page.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSubpageTopBar(title: String) {
	val navController = LocalNavController.current

	TopAppBar(
		{
			Text(title)
		},
		navigationIcon = {
			IconButton({
				navController.popBackStack()
			}) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
			}
		},
	)
}

@Composable
fun NavigateListItem(
	destination: DxrDestination,
	label: String,
	icon: ImageVector,
) {
	val navController = LocalNavController.current

	ListItem(
		{
			Text(label)
		},
		modifier = Modifier
			.clickable {
				navController.navigate(destination.route)
			},
		leadingContent = {
			Icon(icon, null)
		},
	)
}
@Composable
fun InputListItem(
	fillIn: String,
	label: String,
	desc: String,
	alertDesc: String?,
	icon: ImageVector,
	modifier: Modifier = Modifier,
	confirmText: (String) -> Boolean,
) {
	var editing by remember { mutableStateOf(false) }

	if (editing) {
		var text by remember { mutableStateOf(fillIn) }

		AlertDialog(
			{ editing = false },
			{
				TextButton({ editing = !confirmText(text) }) {
					Text(stringResource(R.string.confirm_label))
				}
			},
			dismissButton = {
				TextButton({ editing = false }) {
					Text(stringResource(R.string.dismiss_label))
				}
			},
			icon = {
				Icon(icon, null)
			},
			text = {
				Column {
					if (alertDesc != null) {
						Text(
							alertDesc,
							modifier = Modifier
								.fillMaxWidth(),
							textAlign = TextAlign.Center,
						)
					}
					OutlinedTextField(
						text,
						{ text = it },
						label = {
							Text(label)
						},
					)
				}
			}
		)
	}

	ListItem(
		{
			Text(label)
		},
		modifier = modifier
			.clickable { editing = !editing },
		supportingContent = {
			Text(
				desc,
				overflow = TextOverflow.Ellipsis,
				softWrap = false,
			)
		},
		leadingContent = {
			Icon(icon, null)
		},
	)
}

@Composable
fun ToggleListItem(
	checked: Boolean,
	label: String,
	desc: String,
	icon: ImageVector,
	modifier: Modifier = Modifier,
	check: (Boolean) -> Unit,
) {
	ListItem(
		{
			Text(label)
		},
		modifier = modifier
			.clickable {
				check(!checked)
			},
		supportingContent = {
			Text(desc)
		},
		leadingContent = {
			Icon(icon, null)
		},
		trailingContent = {
			Switch(checked, check)
		}
	)
}

@Composable
fun SingleSelectListItem(
	options: List<String>,
	initialSelection: Int?,
	label: String,
	desc: String,
	icon: ImageVector,
	modifier: Modifier = Modifier,
	confirmSelection: (Int?) -> Boolean,
) {
	var selecting by remember { mutableStateOf(false) }

	if (selecting) {
		var selection by remember { mutableStateOf(initialSelection) }

		AlertDialog(
			onDismissRequest = { selecting = false },
			confirmButton = {
				TextButton({ selecting = !confirmSelection(selection) }) {
					Text(stringResource(R.string.confirm_label))
				}
			},
			dismissButton = {
				TextButton({ selecting = false }) {
					Text(stringResource(R.string.dismiss_label))
				}
			},
			icon = {
				Icon(icon, null)
			},
			title = {
				Text(label)
			},
			text = {
				Column(
					modifier = Modifier
						.selectableGroup(),
				) {
					options.fastForEachIndexed { index, text ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.selectable(
									selected = index == selection,
									onClick = { selection = index },
									role = Role.RadioButton,
								)
								.padding(16.dp),
						) {
							RadioButton(index == selection, null)
							Text(
								text,
								modifier = Modifier
									.padding(start = 16.dp),
								style = MaterialTheme.typography.bodyLarge,
							)
						}
					}
				}
			}
		)
	}

	ListItem(
		{
			Text(label)
		},
		modifier = modifier
			.clickable { selecting = !selecting },
		supportingContent = {
			Text(
				desc,
				overflow = TextOverflow.Ellipsis,
				softWrap = false,
			)
		},
		leadingContent = {
			Icon(icon, null)
		},
	)
}

@Composable
@ReadOnlyComposable
fun settingsValueStringResource(
	value: String?,
	@StringRes valueLabelId: Int = R.string.value_is_label,
	@StringRes nullLabelId: Int = R.string.unset_label,
) = value
	?.let { stringResource(valueLabelId, it) }
	?: stringResource(nullLabelId)
