package com.buhzzi.danxiretainer.page.forum

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.sessionStateDirPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.util.forum.HumanDuration
import kotlinx.coroutines.launch
import kotlin.io.path.createDirectories

@Composable
fun HoleCard(hole: OtHole) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val scope = rememberCoroutineScope()

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)

	val firstFloor = hole.floorsNotNull.firstFloorNotNull
	val lastFloor = hole.floorsNotNull.lastFloorNotNull

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.clickable {
				scope.launch {
					runCatchingOnSnackbar(snackbarController, { it.message ?: unknownErrorLabel }) {
						val userId = checkNotNull(userProfile) { "No user profile" }.userIdNotNull
						context.sessionStateDirPathOf(userId).createDirectories()
						DxrRetention.updateSessionState(userId) {
							copy(
								holeId = hole.holeId,
							)
						}
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
							onClick = {
								scope.launch {
									runCatchingOnSnackbar(snackbarController, { it.message ?: unknownErrorLabel }) {
										val userId = checkNotNull(userProfile) { "No user profile" }.userIdNotNull
										context.sessionStateDirPathOf(userId).createDirectories()
										DxrRetention.updateSessionState(userId) {
											copy(
												holeId = hole.holeId,
											)
										}
										Log.d("System.out", "updateHoleSessionState($userId, ${hole.holeIdNotNull})")
										DxrRetention.updateHoleSessionState(userId, hole.holeIdNotNull) {
											copy(
												pagerFloorIndex = hole.floorsCount.toInt() - 1,
												pagerFloorScrollOffset = 0,
											)
										}
										// optional TODO revert floors or load until the end
									}
								}
							},
							onLongClick = {

							},
						),
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


