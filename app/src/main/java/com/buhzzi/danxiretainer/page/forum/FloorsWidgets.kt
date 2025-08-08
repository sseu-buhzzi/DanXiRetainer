package com.buhzzi.danxiretainer.page.forum

import android.content.ClipData
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalSnackbarController
import com.buhzzi.danxiretainer.util.dxrPrettyJson
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.util.forum.HumanDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun RowScope.FloorsTopBarActions(holeId: Long) {
	val snackbarController = LocalSnackbarController.current

	val scope = rememberCoroutineScope()

	val pagerSharedEventViewModel = viewModel<ChannelPagerSharedEventViewModel>()

	IconButton(
		{
			scope.launch(Dispatchers.IO) {
				runCatchingOnSnackbar(snackbarController) {
					pagerSharedEventViewModel.refreshTrigger.emit(Unit)
				}
			}
		},
	) {
		Icon(Icons.Default.Refresh, null)
	}

	var bottomSheetEvent by remember { mutableStateOf<FloorsBottomSheetEvent?>(null) }

	bottomSheetEvent?.BottomSheet { bottomSheetEvent = it }

	IconButton(
		{
			scope.launch(Dispatchers.IO) {
				runCatchingOnSnackbar(snackbarController) {
					bottomSheetEvent = FloorsBottomSheetEvent.HoleActions(DxrContent.loadHole(holeId))
				}
			}
		},
	) {
		Icon(Icons.Default.MoreVert, null)
	}
}

@Composable
fun FloorCard(floor: OtFloor, hole: OtHole, floorIndex: Int) {
	val context = LocalContext.current

	var bottomSheetEvent by remember { mutableStateOf<FloorsBottomSheetEvent?>(null) }

	bottomSheetEvent?.BottomSheet { bottomSheetEvent = it }

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.combinedClickable(
				onLongClick = {
					bottomSheetEvent = FloorsBottomSheetEvent.FloorActions(floor, floorIndex)
				},
			) {

			},
		shadowElevation = 4.dp,
	) {
		Column(
			modifier = Modifier
				.padding(8.dp),
		) {
			if (floorIndex == 0) {
				TagChipsRow(hole.tagsNotNull)
			}
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Companion.CenterVertically,
			) {
				AnonynameRow(
					floor.anonyname ?: "?",
					floor.anonyname == hole.floors?.firstFloor?.anonyname,
				)
				FloorActionsRow(floor) { bottomSheetEvent = FloorsBottomSheetEvent.FloorActions(floor, floorIndex) }
			}
			// TODO Markdown and mentions
			Text(
				floor.filteredContentNotNull,
			)
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Companion.CenterVertically,
			) {
				val bottomLineColor = MaterialTheme.colorScheme.onSurfaceVariant
				val bottomLineHeight = 12
				Row {
					Text(
						"${floorIndex + 1}F",
						color = bottomLineColor,
						fontSize = bottomLineHeight.sp
					)
					Text(
						"(##${floor.floorIdNotNull})",
						modifier = Modifier
							.padding(4.dp, 0.dp),
						color = bottomLineColor,
						fontSize = (bottomLineHeight - 2).sp,
					)
				}
				floor.modifiedNotNull.toInt().takeIf { it != 0 }?.let { modifiedTimes ->
					Text(
						if (modifiedTimes == 1) {
							stringResource(R.string.modified_label)
						} else {
							stringResource(R.string.modified_times_label, modifiedTimes)
						},
						color = bottomLineColor,
						fontSize = bottomLineHeight.sp,
					)
				}
				Text(
					HumanDuration.tryFormat(context, floor.timeCreatedNotNull.toDateTimeRfc3339()),
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
			}
		}
	}
}

private sealed class FloorsBottomSheetEvent {
	class HoleActions(
		val hole: OtHole,
	) : FloorsBottomSheetEvent() {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (FloorsBottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
				FloorCopyTimeItem(R.string.floor_time_created, hole.timeCreated?.toDateTimeRfc3339())
				FloorCopyTimeItem(R.string.floor_time_updated, hole.timeUpdated?.toDateTimeRfc3339())
				// optional TODO hide if not deleted
				FloorCopyTimeItem(R.string.floor_time_deleted, hole.timeDeleted?.toDateTimeRfc3339())
				HoleCopyJsonItem(hole)
				HoleCopyIndexItem(hole)
				HoleShareAsTextItem(hole)
			}
		}
	}

	class FloorActions(
		val floor: OtFloor,
		val floorIndex: Int,
	) : FloorsBottomSheetEvent() {
		@OptIn(ExperimentalMaterial3Api::class)
		@Composable
		override fun BottomSheet(bottomSheetEventSetter: (FloorsBottomSheetEvent?) -> Unit) {
			ActionsBottomSheet(
				{ bottomSheetEventSetter(null) },
			) {
				FloorCopyTimeItem(R.string.floor_time_created, floor.timeCreated?.toDateTimeRfc3339())
				FloorCopyTimeItem(R.string.floor_time_updated, floor.timeUpdated?.toDateTimeRfc3339())
				FloorCopySelectedItem(floor)
				FloorCopyAllItem(floor)
				FloorCopyJsonItem(floor)
				FloorCopyIndexItem(floor)
				FloorShareAsTextItem(floor, floorIndex)
			}
		}
	}

	@Composable
	abstract fun BottomSheet(bottomSheetEventSetter: (FloorsBottomSheetEvent?) -> Unit)
}

@Composable
private fun FloorCopyTimeItem(labelId: Int, time: OffsetDateTime?) {
	val clipboard = LocalClipboard.current
	val postTimeFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss") }
	val postTimeString = remember(time) {
		time?.format(postTimeFormatter).toString()
	}
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					postTimeString,
				).toClipEntry(),
			)
		},
	) {
		Text(
			stringResource(labelId, postTimeString)
		)
	}
}

@Composable
private fun FloorCopySelectedItem(floor: OtFloor) {
	ClickCatchingActionBottomSheetItem(
		{
			TODO("open the page to select and copy it")
		},
	) {
		Text(stringResource(R.string.floor_copy_selected))
	}
}

@Composable
private fun FloorCopyAllItem(floor: OtFloor) {
	val clipboard = LocalClipboard.current
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					floor.filteredContent.toString(),
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_copy_all))
	}
}

@Composable
private fun HoleCopyJsonItem(hole: OtHole) {
	val clipboard = LocalClipboard.current
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					dxrPrettyJson.encodeToString(hole),
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_copy_json))
	}
}

@Composable
private fun FloorCopyJsonItem(floor: OtFloor) {
	val clipboard = LocalClipboard.current
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					dxrPrettyJson.encodeToString(floor),
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_copy_json))
	}
}

@Composable
private fun HoleCopyIndexItem(hole: OtHole) {
	val clipboard = LocalClipboard.current
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					"#${hole.holeId}",
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_copy_id))
	}
}

@Composable
private fun FloorCopyIndexItem(floor: OtFloor) {
	val clipboard = LocalClipboard.current
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					"##${floor.floorId}",
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_copy_id))
	}
}

@Composable
private fun HoleShareAsTextItem(hole: OtHole) {
	val clipboard = LocalClipboard.current
	val snackbarController = LocalSnackbarController.current
	val floorsFilterContext = LocalFilterContext.current as DxrFloorsFilterContext
	val scope = rememberCoroutineScope()
	val postTimeFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm") }
	ClickCatchingActionBottomSheetItem(
		{
			scope.launch {
				runCatchingOnSnackbar(snackbarController) {
					clipboard.setClipEntry(
						ClipData.newPlainText(
							null,
							buildList {
								DxrContent.floorsFlow(
									hole.holeIdNotNull,
									floorsFilterContext,
								).collect { (floor, _, floorIndex) ->
									add(renderFloorAsText(floor, floorIndex + 1, postTimeFormatter))
								}
							}.joinToString("\n\n"),
						).toClipEntry(),
					)
				}
			}
		},
	) {
		Text(stringResource(R.string.floor_share_as_text))
	}
}

@Composable
private fun FloorShareAsTextItem(floor: OtFloor, floorIndex: Int) {
	val clipboard = LocalClipboard.current
	val postTimeFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm") }
	ClickCatchingActionBottomSheetItem(
		{
			clipboard.setClipEntry(
				ClipData.newPlainText(
					null,
					renderFloorAsText(floor, floorIndex + 1, postTimeFormatter),
				).toClipEntry(),
			)
		},
	) {
		Text(stringResource(R.string.floor_share_as_text))
	}
}

// The ending `.` in below comment comes from `DanXi/lib/page/forum/hole_detail.dart:165:49` at commit `880293e63a3c4762e4c0c6a53438e008aef9330f`
// `index` is `floorIndex + 1`
/// Build the text form of a floor for sharing.
private fun renderFloorAsText(
	floor: OtFloor,
	index: Int,
	postTimeFormatter: DateTimeFormatter,
) = buildString {
	val postTime = floor.timeCreated?.toDateTimeRfc3339()?.format(postTimeFormatter)
	append("${floor.anonyname} 于 $postTime")
	append("${index}F (##${floor.floorId})")
	// TODO implement `renderText` in DanXi
	append(floor.filteredContent ?: "")
}
