package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.MultiChoiceSegmentedButtonRowScope
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import dart.package0.dan_xi.model.forum.OtFloor
import kotlinx.coroutines.launch

@Composable
fun HoleActionsRow(
	modifier: Modifier = Modifier,
	containerHeight: Dp = ActionSegmentedButtonDefaults.containerHeight,
	iconSize: Dp = ActionSegmentedButtonDefaults.iconSize,
	moreAction: () -> Unit,
) {
	MultiChoiceSegmentedButtonRow(
		modifier = modifier
			.height(containerHeight),
	) {
		MoreActionSegmentedButton(
			0, 1,
			iconSize = iconSize,
			action = moreAction,
		)
	}
}

@Composable
fun FloorActionsRow(
	floor: OtFloor,
	modifier: Modifier = Modifier,
	containerHeight: Dp = ActionSegmentedButtonDefaults.containerHeight,
	iconSize: Dp = ActionSegmentedButtonDefaults.iconSize,
	labelSize: TextUnit = ActionSegmentedButtonDefaults.labelSize,
	moreAction: () -> Unit,
) {
	val likeDislikeState = rememberLikeDislikeState(
		floor.likeNotNull.toInt(),
		floor.likedNotNull,
		floor.dislikeNotNull.toInt(),
		floor.dislikedNotNull,
	)
	MultiChoiceSegmentedButtonRow(
		modifier = modifier
			.height(containerHeight),
	) {
		LikeActionSegmentedButton(
			floor.floorIdNotNull,
			likeDislikeState.likeState,
			likeDislikeState,
			0, 3,
			iconSize = iconSize,
			labelSize = labelSize,
		)
		LikeActionSegmentedButton(
			floor.floorIdNotNull,
			likeDislikeState.dislikeState,
			likeDislikeState,
			1, 3,
			iconSize = iconSize,
			labelSize = labelSize,
		)
		MoreActionSegmentedButton(
			2, 3,
			iconSize = iconSize,
			action = moreAction,
		)
	}
}

@Composable
private fun MultiChoiceSegmentedButtonRowScope.LikeActionSegmentedButton(
	floorId: Long,
	likeState: BaseLikeState,
	likeDislikeState: LikeDislikeState,
	index: Int,
	count: Int,
	modifier: Modifier = Modifier,
	iconSize: Dp = ActionSegmentedButtonDefaults.iconSize,
	labelSize: TextUnit = ActionSegmentedButtonDefaults.labelSize,
) {
	val snackbarProvider = LocalSnackbarProvider.current

	val scope = rememberCoroutineScope()

	ActionSegmentedButton(
		likeState.liked,
		{
			scope.launch {
				snackbarProvider.runShowing {
					val updatedFloor = DxrForumApi.likeFloor(floorId, likeState.inverseValue)
					likeDislikeState.updateWith(updatedFloor)
				}
			}
		},
		index,
		count,
		modifier = modifier,
	) {
		Row(
			verticalAlignment = Alignment.Companion.CenterVertically,
		) {
			Icon(
				likeState.icon,
				null,
				modifier = Modifier
					.height(iconSize),
			)
			Text(
				" ${likeState.like}",
				fontSize = labelSize,
			)
		}
	}
}

@Composable
private fun MultiChoiceSegmentedButtonRowScope.MoreActionSegmentedButton(
	index: Int,
	count: Int,
	modifier: Modifier = Modifier,
	iconSize: Dp = ActionSegmentedButtonDefaults.iconSize,
	action: () -> Unit
) {
	ActionSegmentedButton(
		false,
		{
			action()
		},
		index,
		count,
		modifier = modifier,
	) {
		Icon(
			Icons.Default.MoreHoriz,
			null,
			modifier = Modifier
				.height(iconSize),
		)
	}
}

@Composable
private fun MultiChoiceSegmentedButtonRowScope.ActionSegmentedButton(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	index: Int,
	count: Int,
	modifier: Modifier = Modifier,
	label: @Composable () -> Unit,
) {
	SegmentedButton(
		checked,
		onCheckedChange,
		SegmentedButtonDefaults.itemShape(index, count),
		modifier = modifier,
		contentPadding = PaddingValues(0.dp),
		icon = { },
		label = label,
	)
}

object ActionSegmentedButtonDefaults {
	val containerHeight = 24.dp
	val iconSize = 16.dp
	val labelSize = 16.sp
}

@Composable
private fun rememberLikeDislikeState(
	initialLike: Int,
	initialLiked: Boolean,
	initialDislike: Int,
	initialDisliked: Boolean,
): LikeDislikeState {
	return remember {
		LikeDislikeState(
			initialLike,
			initialLiked,
			initialDislike,
			initialDisliked,
		)
	}
}

private class LikeDislikeState(
	initialLike: Int,
	initialLiked: Boolean,
	initialDislike: Int,
	initialDisliked: Boolean,
) {
	val likeState = LikeState(initialLike, initialLiked)
	val dislikeState = DislikeState(initialDislike, initialDisliked)

	fun updateWith(floor: OtFloor) {
		likeState.updateWith(floor)
		dislikeState.updateWith(floor)
	}
}

private class LikeState(
	initialLike: Int,
	initialLiked: Boolean,
) : BaseLikeState(
	initialLike,
	initialLiked,
) {
	override val icon
		get() = if (liked) {
			Icons.Default.ThumbUpOffAlt
		} else {
			Icons.Default.ThumbUpAlt
		}

	override val inverseValue: Long
		get() = if (liked) 0 else 1

	override fun updateWith(floor: OtFloor) {
		floor.like?.let { like = it.toInt() }
		floor.liked?.let { liked = it }
	}
}

private class DislikeState(
	initialDislike: Int,
	initialDisliked: Boolean,
) : BaseLikeState(
	initialDislike,
	initialDisliked,
) {
	override val icon
		get() = if (liked) {
			Icons.Default.ThumbDownOffAlt
		} else {
			Icons.Default.ThumbDownAlt
		}

	override val inverseValue: Long
		get() = if (liked) 0 else -1

	override fun updateWith(floor: OtFloor) {
		floor.dislike?.let { like = it.toInt() }
		floor.disliked?.let { liked = it }
	}
}

private abstract class BaseLikeState(
	initialLike: Int,
	initialLiked: Boolean,
) {
	var like by mutableIntStateOf(initialLike)
		protected set
	var liked by mutableStateOf(initialLiked)
		protected set

	abstract val icon: ImageVector

	/**
	 * When calling [DxrForumApi.likeFloor], use `1` to like, `-1` to dislike, and `0` to cancel.
	 */
	abstract val inverseValue: Long

	abstract fun updateWith(floor: OtFloor)
}
