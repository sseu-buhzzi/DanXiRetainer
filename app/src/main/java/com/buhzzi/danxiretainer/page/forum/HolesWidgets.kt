package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.floorsReversed
import com.buhzzi.danxiretainer.repository.settings.floorsReversedOrDefault
import com.buhzzi.danxiretainer.repository.settings.floorsReversedOrDefaultFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.util.forum.HumanDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

@Suppress("UnusedReceiverParameter")
@Composable
fun RowScope.HolesTopBarActions() {
	// TODO view switching
	GotoFloorsButton()
	RefreshButton()
}

@Composable
private fun RefreshButton() {
	val snackbarProvider = LocalSnackbarProvider.current
	val scope = rememberCoroutineScope()
	val pagerSharedEventViewModel = viewModel<ChannelPagerSharedEventViewModel>()
	IconButton(
		{
			scope.launch(Dispatchers.IO) {
				snackbarProvider.runShowingSuspend {
					pagerSharedEventViewModel.refreshTrigger.emit(Unit)
				}
			}
		},
	) {
		Icon(Icons.Default.Refresh, null)
	}
}

@Composable
private fun GotoFloorsButton() {
	var entering by remember { mutableStateOf(false) }

	if (entering) {
		val snackbarProvider = LocalSnackbarProvider.current
		val scope = rememberCoroutineScope()
		var holeIdString by remember { mutableStateOf("") }

		AlertDialog(
			{ entering = false },
			{
				TextButton(
					gotoHole@{
						val holeId = holeIdString
							.substringAfterLast('#')
							.toLongOrNull()
							?: return@gotoHole
						scope.launch(Dispatchers.IO) {
							snackbarProvider.runShowingSuspend {
								openFloors(holeId)
							}
						}
						entering = false
					},
				) {
					Text(stringResource(R.string.confirm_label))
				}
			},
			icon = {
				Icon(Icons.Default.MyLocation, null)
			},
			text = {
				Column {
					Text(
						stringResource(R.string.floors_goto_hole_id),
						modifier = Modifier
							.fillMaxWidth(),
						textAlign = TextAlign.Center,
					)
					OutlinedTextField(
						holeIdString,
						{ holeIdString = it },
					)
				}
			},
		)
		return
	}

	IconButton(
		{ entering = true },
	) {
		Icon(Icons.Default.MyLocation, null)
	}
}


@Composable
fun HoleCard(hole: OtHole) {
	val context = LocalContext.current
	val snackbarProvider = LocalSnackbarProvider.current

	val scope = rememberCoroutineScope()

	var bottomSheetEvent by remember { mutableStateOf<HolesBottomSheetEvent?>(null) }

	bottomSheetEvent?.BottomSheet { bottomSheetEvent = it }

	val reversed by DxrSettings.Models.floorsReversedOrDefaultFlow.collectAsState(
		DxrSettings.Models.floorsReversedOrDefault,
	)

	val firstFloor = hole.floorsNotNull.firstFloorNotNull
	val lastFloor = hole.floorsNotNull.lastFloorNotNull

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.combinedClickable(
				onLongClick = {
					bottomSheetEvent = HolesBottomSheetEvent.HoleActions(hole)
				},
			) {
				scope.launch(Dispatchers.IO) {
					snackbarProvider.runShowingSuspend {
						openFloors(hole.holeIdNotNull)
					}
				}
			},
		shadowElevation = 4.dp,
	) {
		Column(
			modifier = Modifier
				.padding(8.dp),
		) {
			TagChipsRow(hole.tagsNotNull)
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				AnonynameRow(
					firstFloor, hole, 0,
					modifier = Modifier.weight(1F),
				)
				HoleActionsRow { bottomSheetEvent = HolesBottomSheetEvent.HoleActions(hole) }
			}
			MarkdownContentRenderer(
				firstFloor.filteredContentNotNull,
			)
			if (lastFloor != firstFloor) {
				Row(
					modifier = Modifier
						.height(IntrinsicSize.Min)
						.padding(4.dp)
						.combinedClickable(
							onLongClick = { bottomSheetEvent = HolesBottomSheetEvent.LastFloorActions(hole) },
						) {
							scope.launch(Dispatchers.IO) {
								snackbarProvider.runShowingSuspend {
									val options = if (reversed) {
										OpenFloorsOptions.REVERSED_ENDING
									} else {
										OpenFloorsOptions.NORMAL_ENDING
									}
									openFloorsAtEnding(
										hole.holeIdNotNull,
										hole.floorsCount.toInt(),
										options,
									)
								}
							}
						},
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.Companion.CenterVertically,
				) {
					val replyColor = MaterialTheme.colorScheme.onSurfaceVariant
					VerticalDivider(
						modifier = Modifier
							.padding(4.dp),
						thickness = 4.dp,
						color = replyColor,
					)
					Column(
						modifier = Modifier
							.fillMaxWidth(),
					) {
						Text(
							stringResource(
								R.string.replied_on_label,
								lastFloor.anonyname ?: "?",
								HumanDuration.tryFormat(context, lastFloor.timeCreatedNotNull.toDateTimeRfc3339()),
							),
							color = replyColor,
						)
						Text(
							lastFloor.filteredContentNotNull,
							color = replyColor,
							fontSize = 14.sp,
						)
					}
				}
			}
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Companion.CenterVertically,
			) {
				val bottomLineColor = MaterialTheme.colorScheme.onSurfaceVariant
				val bottomLineHeight = 12
				Text(
					"#${hole.holeIdNotNull}",
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
				Text(
					HumanDuration.tryFormat(context, hole.timeCreatedNotNull.toDateTimeRfc3339()),
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
				Row(
					verticalAlignment = Alignment.Companion.CenterVertically,
				) {
					Text(
						"${hole.replyNotNull} ",
						color = bottomLineColor,
						fontSize = bottomLineHeight.sp,
					)
					Icon(
						Icons.Default.Sms, null,
						modifier = Modifier
							.height(bottomLineHeight.dp),
					)
				}
			}
		}
	}
}


private sealed class HolesBottomSheetEvent(
	val hole: OtHole,
) {
	class HoleActions(hole: OtHole) : HolesBottomSheetEvent(hole) {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (HolesBottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
				OpenFloorsInOrderItem(hole, OpenFloorsOptions.NORMAL_BEGINNING)
				OpenFloorsInOrderItem(hole, OpenFloorsOptions.REVERSED_BEGINNING)
				ClickCatchingActionBottomSheetItem(
					{
						bottomSheetEventSetter(LastFloorActions(hole))
					},
				) {
					Text(stringResource(R.string.floors_order_label))
				}
			}
		}
	}

	class LastFloorActions(hole: OtHole) : HolesBottomSheetEvent(hole) {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (HolesBottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
				OpenFloorsInOrderItem(hole, OpenFloorsOptions.NORMAL_ENDING)
				OpenFloorsInOrderItem(hole, OpenFloorsOptions.REVERSED_ENDING)
				ToggleFloorsOrderItem()
			}
		}
	}

	@Composable
	abstract fun BottomSheet(bottomSheetEventSetter: (HolesBottomSheetEvent?) -> Unit)
}

@Composable
private fun OpenFloorsInOrderItem(hole: OtHole, options: OpenFloorsOptions) {
	val reversed by DxrSettings.Models.floorsReversedOrDefaultFlow.collectAsState(
		DxrSettings.Models.floorsReversedOrDefault,
	)

	ClickCatchingActionBottomSheetItem(
		{
			openFloorsAtEnding(
				hole.holeIdNotNull,
				hole.floorsCount.toInt(),
				options,
			)
		},
	) {
		Text(buildString {
			append(
				when (options) {
					OpenFloorsOptions.NORMAL_ENDING -> stringResource(R.string.open_in_normal_order_at_ending_label)
					OpenFloorsOptions.NORMAL_BEGINNING -> stringResource(R.string.open_in_normal_order_at_beginning_label)
					OpenFloorsOptions.REVERSED_ENDING -> stringResource(R.string.open_in_reversed_order_at_ending_label)
					OpenFloorsOptions.REVERSED_BEGINNING -> stringResource(R.string.open_in_reversed_order_at_beginning_label)
				},
			)
			if (options.reversed == reversed) {
				append(stringResource(R.string.floors_order_current_label))
			}
		})
	}
}

@Composable
private fun ToggleFloorsOrderItem() {
	val reversed by DxrSettings.Models.floorsReversedOrDefaultFlow.collectAsState(
		DxrSettings.Models.floorsReversedOrDefault,
	)

	ClickCatchingActionBottomSheetItem(
		{
			DxrSettings.Prefs.floorsReversed = !reversed
		},
	) {
		Text(
			if (reversed) {
				stringResource(R.string.change_floors_order_to_normal_label)
			} else {
				stringResource(R.string.change_floors_order_to_reversed_label)
			},
		)
	}
}

private fun openFloorsAtEnding(holeId: Long, floorsCount: Int, options: OpenFloorsOptions) {
	val pagerFloorIndex = if (options.reversed xor options.beginning) 0 else floorsCount - 1
	openFloors(
		holeId,
		reversed = options.reversed,
		pagerFloorIndex = pagerFloorIndex,
		pagerFloorScrollOffset = 0,
		refreshTime = OffsetDateTime.now(),
	)
}

private fun openFloors(
	holeId: Long,
	reversed: Boolean? = null,
	pagerFloorIndex: Int? = null,
	pagerFloorScrollOffset: Int? = null,
	refreshTime: OffsetDateTime? = null,
) {
	val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
	DxrRetention.updateSessionState(userId) {
		copy(
			holeId = holeId,
		)
	}
	DxrRetention.updateHoleSessionState(userId, holeId) {
		copy(
			reversed = reversed ?: this.reversed,
			pagerFloorIndex = pagerFloorIndex ?: this.pagerFloorIndex,
			pagerFloorScrollOffset = pagerFloorScrollOffset ?: this.pagerFloorScrollOffset,
			refreshTime = refreshTime?.toStringRfc3339() ?: this.refreshTime,
		)
	}
}

private enum class OpenFloorsOptions(
	val reversed: Boolean,
	val beginning: Boolean,
) {
	NORMAL_ENDING(false, false),
	NORMAL_BEGINNING(false, true),
	REVERSED_ENDING(true, false),
	REVERSED_BEGINNING(true, true);
}
