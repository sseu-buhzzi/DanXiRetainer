package com.buhzzi.danxiretainer.page.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import dart.package0.dan_xi.util.forum.HumanDuration

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
				verticalAlignment = Alignment.Companion.CenterVertically,
			) {
				AnonynameRow(
					floor.anonyname ?: "?",
					floor.anonyname == hole.floors?.firstFloor?.anonyname,
				)
				FloorActionsRow(floor)
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