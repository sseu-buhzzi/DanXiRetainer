package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.floorsReversed
import com.buhzzi.danxiretainer.repository.settings.floorsReversedFlow
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.util.forum.HumanDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoleCard(hole: OtHole) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val scope = rememberCoroutineScope()

	var bottomSheetEvent by remember { mutableStateOf<BottomSheetEvent?>(null) }

	bottomSheetEvent?.BottomSheet { bottomSheetEvent = it }

	val reversed by DxrSettings.Items.floorsReversedFlow.collectAsState(null)

	val firstFloor = hole.floorsNotNull.firstFloorNotNull
	val lastFloor = hole.floorsNotNull.lastFloorNotNull

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.combinedClickable(
				onLongClick = {
					bottomSheetEvent = BottomSheetEvent.HoleActions(hole)
				},
			) {
				scope.launch(Dispatchers.IO) {
					runCatchingOnSnackbar(snackbarController) {
						openFloors(hole)
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
				AnonynameRow(firstFloor.anonyname ?: "?", true)
				HoleActionsRow()
			}
			Text(
				firstFloor.filteredContentNotNull,
			)
			if (lastFloor != firstFloor) {
				Row(
					modifier = Modifier
						.height(IntrinsicSize.Min)
						.padding(4.dp)
						.combinedClickable(
							onLongClick = {
								bottomSheetEvent = BottomSheetEvent.LastFloorActions(hole)
							},
						) {
							scope.launch(Dispatchers.IO) {
								runCatchingOnSnackbar(snackbarController) {
									openFloorsAtNewest(hole, reversed == true)
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

private sealed class BottomSheetEvent(
	val hole: OtHole,
) {
	// feat TODO hole actions can provide an access to last floor actions
	class HoleActions(hole: OtHole) : BottomSheetEvent(hole) {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (BottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
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

	// feat TODO open to last, open in reversed order, change the order, etc.
	class LastFloorActions(hole: OtHole) : BottomSheetEvent(hole) {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (BottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
				OpenFloorsInOrderItem(hole, false)
				OpenFloorsInOrderItem(hole, true)
				ToggleFloorsOrderItem()
			}
		}
	}

	@Composable
	abstract fun BottomSheet(bottomSheetEventSetter: (BottomSheetEvent?) -> Unit)
}

@Composable
private fun OpenFloorsInOrderItem(hole: OtHole, reversed: Boolean) {
	val reversedInSettings by DxrSettings.Items.floorsReversedFlow.collectAsState(null)

	ClickCatchingActionBottomSheetItem(
		{
			openFloorsAtNewest(hole, reversed)
		},
	) {
		Text(buildString {
			append(
				if (reversed) {
					stringResource(R.string.open_in_reversed_order_at_newest_label)
				} else {
					stringResource(R.string.open_in_normal_order_at_newest_label)
				},
			)
			if (reversed == reversedInSettings) {
				append(stringResource(R.string.floors_order_current_label))
			}
		})
	}
}

@Composable
private fun ToggleFloorsOrderItem() {
	val reversed by DxrSettings.Items.floorsReversedFlow.collectAsState(null)

	ClickCatchingActionBottomSheetItem(
		{
			DxrSettings.Items.floorsReversed = reversed != true
		},
	) {
		Text(
			if (reversed == true) {
				stringResource(R.string.change_floors_order_to_normal_label)
			} else {
				stringResource(R.string.change_floors_order_to_reversed_label)
			},
		)
	}
}

private fun openFloorsAtNewest(
	hole: OtHole,
	reversed: Boolean,
) {
	if (reversed) {
		openFloors(
			hole,
			reversed = true,
			pagerFloorIndex = 0,
			pagerFloorScrollOffset = 0,
			forumApiRefreshTime = OffsetDateTime.now(),
		)
	} else {
		openFloors(
			hole,
			reversed = false,
			pagerFloorIndex = hole.floorsCount.toInt() - 1,
			pagerFloorScrollOffset = 0,
			forumApiRefreshTime = OffsetDateTime.now(),
		)
	}
}

private fun openFloors(
	hole: OtHole,
	reversed: Boolean? = null,
	pagerFloorIndex: Int? = null,
	pagerFloorScrollOffset: Int? = null,
	forumApiRefreshTime: OffsetDateTime? = null,
) {
	val userId = checkNotNull(DxrSettings.Models.userProfile) { "No user profile" }.userIdNotNull
	DxrRetention.updateSessionState(userId) {
		copy(
			holeId = hole.holeId,
		)
	}
	DxrRetention.updateHoleSessionState(userId, hole.holeIdNotNull) {
		copy(
			reversed = reversed ?: this.reversed,
			pagerFloorIndex = pagerFloorIndex ?: this.pagerFloorIndex,
			pagerFloorScrollOffset = pagerFloorScrollOffset ?: this.pagerFloorScrollOffset,
			forumApiRefreshTime = forumApiRefreshTime?.toStringRfc3339() ?: this.forumApiRefreshTime,
		)
	}
}
