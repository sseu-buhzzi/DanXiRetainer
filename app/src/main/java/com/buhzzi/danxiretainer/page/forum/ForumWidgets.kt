package com.buhzzi.danxiretainer.page.forum

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.retension.RetentionPageContent
import com.buhzzi.danxiretainer.page.retension.RetentionPageTopBar
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.page.settings.SettingsPageContent
import com.buhzzi.danxiretainer.page.settings.SettingsPageTopBar
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.sessionStateDirPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.dan_xi.model.forum.OtFloor
import dart.dan_xi.model.forum.OtHole
import dart.dan_xi.model.forum.OtTag
import dart.dan_xi.util.forum.HumanDuration
import dart.dan_xi.util.hashColor
import dart.dan_xi.util.lightness
import dart.dan_xi.util.withLightness
import dart.flutter.src.material.Colors
import kotlinx.coroutines.launch
import kotlin.io.path.createDirectories

enum class ForumPages(
	val icon: @Composable () -> Unit,
	val label: @Composable () -> Unit,
	val topBar: @Composable () -> Unit,
	val content: @Composable (Modifier) -> Unit,
) {
	FORUM(
		{
			Icon(Icons.Default.Forum, null)
		},
		{
			Text(stringResource(R.string.forum_label))
		},
		{
			ForumPageTopBar()
		},
		{ modifier ->
			ForumPageContent(
				modifier = modifier,
			)
		},
	),
	RETENTION(
		{
			Icon(Icons.Default.Storage, null)
		},
		{
			Text(stringResource(R.string.retention_label))
		},
		{
			RetentionPageTopBar()
		},
		{ modifier ->
			RetentionPageContent(
				modifier = modifier,
			)
		},
	),
	SETTINGS(
		{
			Icon(Icons.Default.Settings, null)
		},
		{
			Text(stringResource(R.string.settings_label))
		},
		{
			SettingsPageTopBar()
		},
		{ modifier ->
			SettingsPageContent(
				modifier = modifier,
			)
		},
	),
}

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
			AnonynameRow(firstFloor.anonyname ?: "?", true)
			Text(
				firstFloor.filteredContentNotNull,
			)
			if (lastFloor != firstFloor) {
				Row(
					modifier = Modifier
						.height(IntrinsicSize.Min)
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
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					verticalAlignment = Alignment.CenterVertically,
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
				verticalAlignment = Alignment.CenterVertically,
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
					verticalAlignment = Alignment.CenterVertically,
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

@Composable
fun FloorCard(floor: OtFloor, hole: OtHole, floorIndex: Int) {
	val context = LocalContext.current

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.clickable {

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
				verticalAlignment = Alignment.CenterVertically,
			) {
				AnonynameRow(
					floor.anonyname ?: "?",
					floor.anonyname == hole.floors?.firstFloor?.anonyname,
				)
				ActionsRow(floor)
			}
			// TODO Markdown and mentions
			Text(
				floor.filteredContentNotNull,
			)
			Row(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
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

@Composable
fun TagChipsRow(tags: List<OtTag>) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth(),
	) {
		tags.forEach { tag ->
			// TODO 从static toml中獲取highlighted tag names
			TagChip(tag, tag.name == "highlighted")
		}
	}
}

@Composable
fun TagChip(
	tag: OtTag,
	highlighted: Boolean = false,
) {
	val systemInDarkTheme = isSystemInDarkTheme()

	val shape = RoundedCornerShape(2.dp)
	val modifier = Modifier
		.padding(2.dp)
		.run {
			if (highlighted) {
				background(Brush.horizontalGradient(listOf(Colors.blue, Colors.purple).map { materialColor ->
					materialColor.color.copy(0.875F)
				}), shape)
			} else {
				this
			}
		}
		.clickable {
			// TODO 將tag加入篩選器
		}
	val (color, contentColor) = if (highlighted) {
		Color.Transparent to Color.White
	} else {
		val effectiveColor = tag.getColor(systemInDarkTheme)
		effectiveColor.copy(0.5F) to effectiveColor.withLightness { (_, _, lightness) ->
			if (systemInDarkTheme) {
				lightness + 0.125F
			} else {
				lightness - 0.125F
			}.coerceIn(0F, 1F)
		}
	}
	Surface(
		modifier = modifier,
		shape = shape,
		color = color,
		contentColor = contentColor,
	) {
		Text(
			checkNotNull(tag.name) {
				tag
			},
			modifier = Modifier
				.padding(4.dp, 0.dp),
		)
	}
}

@Composable
fun AnonynameRow(anonyname: String, posterOriginal: Boolean) {
	Row(
		modifier = Modifier
			.height(IntrinsicSize.Min)
			.padding(4.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		val systemInDarkTheme = isSystemInDarkTheme()
		val anonynameColor = anonyname.hashColor(systemInDarkTheme) ?: Color.Red
		VerticalDivider(
			modifier = Modifier
				.padding(4.dp),
			thickness = 4.dp,
			color = anonynameColor,
		)
		if (posterOriginal) {
			Surface(
				modifier = Modifier
					.padding(2.dp),
				shape = RoundedCornerShape(2.dp),
				color = anonynameColor,
				contentColor = if (anonynameColor.lightness < 0.5) Color.White else Color.Black,
			) {
				Text(
					// TODO 可選項LZ・DZ・或OP
					"LZ",
					modifier = Modifier
						.padding(4.dp, 0.dp),
					fontSize = 14.sp,
					fontWeight = FontWeight.Bold,
					lineHeight = 16.sp,
				)
			}
		}
		Text(
			anonyname,
			color = anonynameColor,
			fontWeight = FontWeight.Bold,
		)
	}
}

@Composable
fun ActionsRow(
	floor: OtFloor,
	modifier: Modifier = Modifier,
	rowHeight: Int = 24,
	iconHeight: Int = 16,
) {
	val snackbarController = LocalSnackbarController.current

	val scope = rememberCoroutineScope()

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	var like by remember { mutableLongStateOf(floor.likeNotNull) }
	var liked by remember { mutableStateOf(floor.likedNotNull) }
	var dislike by remember { mutableLongStateOf(floor.dislikeNotNull) }
	var disliked by remember { mutableStateOf(floor.dislikedNotNull) }
	MultiChoiceSegmentedButtonRow(
		modifier = modifier
			.height(rowHeight.dp),
	) {
		SegmentedButton(
			liked,
			{
				scope.launch {
					runCatchingOnSnackbar(snackbarController, { it.message ?: unknownErrorLabel }) {
						val updatedFloor = DxrForumApi.likeFloor(
							floor.floorIdNotNull,
							if (liked) 0 else 1,
						)
						updatedFloor.like?.let { like = it }
						updatedFloor.liked?.let { liked = it }
						updatedFloor.dislike?.let { dislike = it }
						updatedFloor.disliked?.let { disliked = it }
					}
				}
			},
			SegmentedButtonDefaults.itemShape(0, 3),
			contentPadding = PaddingValues(0.dp),
			icon = { },
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					if (liked) {
						Icons.Default.ThumbUpAlt
					} else {
						Icons.Default.ThumbUpOffAlt
					},
					null,
					modifier = Modifier
						.height(iconHeight.dp),
				)
				Text(
					" $like",
					fontSize = iconHeight.sp,
				)
			}
		}
		SegmentedButton(
			disliked,
			{
				scope.launch {
					runCatchingOnSnackbar(snackbarController, { it.message ?: unknownErrorLabel }) {
						val updatedFloor = DxrForumApi.likeFloor(
							floor.floorIdNotNull,
							if (disliked) 0 else -1,
						)
						updatedFloor.like?.let { like = it }
						updatedFloor.liked?.let { liked = it }
						updatedFloor.dislike?.let { dislike = it }
						updatedFloor.disliked?.let { disliked = it }
					}
				}
			},
			SegmentedButtonDefaults.itemShape(1, 3),
			contentPadding = PaddingValues(0.dp),
			icon = { },
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					if (disliked) {
						Icons.Default.ThumbDownAlt
					} else {
						Icons.Default.ThumbDownOffAlt
					},
					null,
					modifier = Modifier
						.height(iconHeight.dp),
				)
				Text(
					" $dislike",
					fontSize = iconHeight.sp,
				)
			}
		}
		SegmentedButton(
			false,
			{

			},
			SegmentedButtonDefaults.itemShape(2, 3),
			contentPadding = PaddingValues(0.dp),
		) {
			Icon(
				Icons.Default.MoreHoriz, null,
				modifier = Modifier
					.height(iconHeight.dp),
			)
		}
	}
}
