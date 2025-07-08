package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
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
import com.buhzzi.danxiretainer.page.runBlockingOrShowSnackbarMessage
import com.buhzzi.danxiretainer.page.settings.SettingsPageContent
import com.buhzzi.danxiretainer.page.settings.SettingsPageTopBar
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.sessionStateDirPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import dart.dan_xi.model.forum.OtFloor
import dart.dan_xi.model.forum.OtHole
import dart.dan_xi.model.forum.OtTag
import dart.dan_xi.util.forum.HumanDuration
import dart.dan_xi.util.hashColor
import dart.dan_xi.util.withLightness
import dart.flutter.src.material.Colors
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
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

	val firstFloor = checkNotNull(hole.floors?.firstFloor) {
		hole
	}
	val lastFloor = checkNotNull(hole.floors.lastFloor) {
		hole
	}

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.clickable {
				scope.launch {
					runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
						val userId = checkNotNull(userProfile?.userId)
						context.sessionStateDirPathOf(userId).createDirectories()
						DxrRetention.updateSessionState(userId) {
							copy(
								holeId = hole.holeId,
								holeInitialFloorRank = 0,
								forumApiTimeOfHoles = OffsetDateTime.now().toStringRfc3339(),
							)
						}
					}
				}
			},
		// TODO 驗證在有圖片背景時顯示正常
		color = MaterialTheme.colorScheme.surface.copy(0.875F),
		shadowElevation = 4.dp,
	) {
		Column(
			modifier = Modifier
				.padding(8.dp),
		) {
			TagChipRow(checkNotNull(hole.tags) {
				hole
			})
			Row(
				modifier = Modifier
					.height(IntrinsicSize.Min)
					.padding(4.dp),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				val systemInDarkTheme = isSystemInDarkTheme()
				val anonynameColor = checkNotNull(firstFloor.anonyname) {
					hole
				}.hashColor(systemInDarkTheme) ?: Color.Red
				VerticalDivider(
					modifier = Modifier
						.padding(4.dp),
					thickness = 4.dp,
					color = anonynameColor,
				)
				Text(
					firstFloor.anonyname,
					color = anonynameColor,
					fontWeight = FontWeight.Bold,
				)
			}
			Text(
				checkNotNull(firstFloor.filteredContent) {
					hole
				},
			)
			if (lastFloor != firstFloor) {
				Row(
					modifier = Modifier
						.height(IntrinsicSize.Min)
						.padding(4.dp)
						.clickable {
							scope.launch {
								runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
									val userId = checkNotNull(userProfile?.userId)
									context.sessionStateDirPathOf(userId).createDirectories()
									DxrRetention.updateSessionState(userId) {
										copy(
											holeId = hole.holeId,
											holeInitialFloorRank = hole.floorsCount + 1,
											forumApiTimeOfHoles = OffsetDateTime.now().toStringRfc3339(),
										)
									}
									// TODO 進入尾樓
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
								checkNotNull(lastFloor.anonyname) {
									hole
								},
								HumanDuration.tryFormat(context, checkNotNull(lastFloor.timeCreated) {
									hole
								}.toDateTimeRfc3339()),
							),
							color = replyColor,
						)
						Text(
							checkNotNull(lastFloor.filteredContent) {
								hole
							},
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
					"#${
						checkNotNull(hole.holeId) {
							hole
						}
					}",
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
				Text(
					HumanDuration.tryFormat(context, checkNotNull(hole.timeCreated) {
						hole
					}.toDateTimeRfc3339()),
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						"${
							checkNotNull(hole.reply) {
								hole
							}
						} ",
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
fun FloorCard(floor: OtFloor, hole: OtHole, rank: Int) {
	val context = LocalContext.current

	Surface(
		modifier = Modifier
			.padding(4.dp)
			.clickable {

			},
		// TODO 驗證在有圖片背景時顯示正常
		color = MaterialTheme.colorScheme.surface.copy(0.875F),
		shadowElevation = 4.dp,
	) {
		Column(
			modifier = Modifier
				.padding(8.dp),
		) {
			Row(
				modifier = Modifier
					.height(IntrinsicSize.Min)
					.padding(4.dp),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				val systemInDarkTheme = isSystemInDarkTheme()
				val anonynameColor = checkNotNull(floor.anonyname) {
					floor
				}.hashColor(systemInDarkTheme) ?: Color.Red
				VerticalDivider(
					modifier = Modifier
						.padding(4.dp),
					thickness = 4.dp,
					color = anonynameColor,
				)
				if (floor.anonyname == hole.floors?.firstFloor?.anonyname) {
					Surface(
						modifier = Modifier
							.padding(2.dp),
						shape = RoundedCornerShape(2.dp),
						color = anonynameColor,
						contentColor = Color.White,
					) {
						Text(
							// TODO LZ或DZ可選項
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
					floor.anonyname,
					color = anonynameColor,
					fontWeight = FontWeight.Bold,
				)
			}
			Text(
				checkNotNull(floor.filteredContent) {
					floor
				},
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
						"${rank + 1}F",
						color = bottomLineColor,
						fontSize = bottomLineHeight.sp
					)
					Text(
						"(##${
							checkNotNull(floor.floorId) {
								floor
							}
						})",
						modifier = Modifier
							.padding(4.dp, 0.dp),
						color = bottomLineColor,
						fontSize = (bottomLineHeight - 2).sp,
					)
				}
				checkNotNull(floor.modified).toInt().takeIf { it != 0 }?.let { modifiedTimes ->
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
					HumanDuration.tryFormat(context, checkNotNull(floor.timeCreated) {
						floor
					}.toDateTimeRfc3339()),
					color = bottomLineColor,
					fontSize = bottomLineHeight.sp,
				)
			}
		}
	}
}

@Composable
fun TagChipRow(tags: List<OtTag>) {
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
