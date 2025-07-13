package com.buhzzi.danxiretainer.page.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.settings.DxrContentSource
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathFlow
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefaultFlow
import com.buhzzi.danxiretainer.repository.settings.sortOrderOrDefault
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.floorIndicesPathOf
import com.buhzzi.danxiretainer.util.holeIndicesPathOf
import com.buhzzi.danxiretainer.util.holeSessionStatePathOf
import com.buhzzi.danxiretainer.util.sessionStateDirPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import com.buhzzi.danxiretainer.util.updateWith
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import kotlin.io.path.getLastModifiedTime

val LocalSessionState = compositionLocalOf<DxrSessionState> {
	error("LocalSessionState not provided")
}

@Composable
fun ForumPage() {
	val context = LocalContext.current

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)
	// optional TODO this kind of `return`s can be more user-friendly loading information boxes
	val userId = userProfile?.userId ?: return

	// TODO 移除其他trigger, replace them with unified entrance function
	val sessionStateNullable by produceState<DxrSessionState?>(null, userId) {
		val sessionStateCurrentPath = context.sessionStateDirPathOf(userId)
		updateWith(listOf(sessionStateCurrentPath.toFile())) {
			DxrRetention.loadSessionState(userId)
		}
	}
	val sessionState = sessionStateNullable ?: return

	var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
	val selectedPage = remember(selectedIndex) { ForumPages.entries[selectedIndex] }

	CompositionLocalProvider(LocalSessionState provides sessionState) {
		DxrScaffoldWrapper(
			topBar = {
				selectedPage.topBar()
			},
			bottomBar = {
				NavigationBar {
					ForumPages.entries.forEachIndexed { index, page ->
						NavigationBarItem(
							selected = index == selectedIndex,
							onClick = {
								selectedIndex = index
							},
							icon = page.icon,
							label = page.label,
						)
					}
				}
			},
		) { contentPaddings ->
			selectedPage.content(
				Modifier
					.padding(contentPaddings),
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumPageTopBar() {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current

	val scope = rememberCoroutineScope()

	TopAppBar(
		{
			sessionState.holeId?.let { holeId ->
				Text("#$holeId")
			} ?: Text(stringResource(R.string.forum_label))
		},
		navigationIcon = {
			if (sessionState.holeId != null) {
				fun goBackToForumHolesPage() {
					scope.launch(Dispatchers.IO) {
						runCatchingOnSnackbar(snackbarController) {
							val userId = DxrSettings.Models.userProfileNotNull.userIdNotNull
							DxrRetention.updateSessionState(userId) {
								copy(
									holeId = null,
								)
							}
						}
					}
				}
				BackHandler {
					goBackToForumHolesPage()
				}

				IconButton({
					goBackToForumHolesPage()
				}) {
					Icon(Icons.Default.Home, null)
				}
			}
		},
		actions = {
			IconButton({

			}) {
				Icon(Icons.Default.RemoveRedEye, null)
			}

			var expanded by remember { mutableStateOf(false) }
			Box {
				OutlinedButton(
					{
						expanded = !expanded
					},
					modifier = Modifier
						.padding(0.dp),
				) {
					Text(
						"Main view",
						modifier = Modifier
							.padding(0.dp)
							.fillMaxWidth(0.25F),
						overflow = TextOverflow.Ellipsis,
						softWrap = false,
					)
				}
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					repeat(4) {
						DropdownMenuItem(
							text = { Text("Option $it. Long description .................") },
							onClick = { /* Do something... */ }
						)
					}
				}
			}
		}
	)
}

@Composable
fun ForumPageContent(modifier: Modifier = Modifier) {
	val sessionState = LocalSessionState.current

	val userProfileNullable by DxrSettings.Models.userProfileFlow.collectAsState(null)
	val contentSource by DxrSettings.Models.contentSourceOrDefaultFlow.collectAsState(
		DxrSettings.Models.contentSourceOrDefault,
	)

	Box(
		modifier = modifier,
	) {
		val backgroundImagePathString by DxrSettings.Models.backgroundImagePathFlow.collectAsState(null)
		backgroundImagePathString?.toFile()?.let { backgroundImageFile ->
			Image(
				rememberAsyncImagePainter(backgroundImageFile),
				null,
				modifier = Modifier
					.matchParentSize(),
				contentScale = ContentScale.Crop,
			)
		}

		val userProfile = userProfileNullable ?: return
		val userId = userProfile.userId ?: return
		when (contentSource) {
			DxrContentSource.FORUM_API -> sessionState.holeId?.let { currentHoleId ->
				ForumApiFloorsPager(userId, currentHoleId)
			} ?: ForumApiHolesPager(userId)

			DxrContentSource.RETENTION -> sessionState.holeId?.let { currentHoleId ->
				RetentionFloorsPager(userId, currentHoleId)
			} ?: RetentionHolesPager(userId)
		}
	}
}

@Composable
private fun ForumApiHolesPager(userId: Long) {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current
	val refreshTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	ChannelPager(
		{
			runCatchingOnSnackbar(snackbarController) {
				sendForumApiHoles(refreshTime)
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "ForumApiHolesPager")
			put("userId", userId)
			put("refreshTime", refreshTime.toStringRfc3339())
		}),
		16,
		sessionState.pagerHoleIndex ?: 0,
		sessionState.pagerHoleScrollOffset ?: 0,
		getHolePositionSaver(userId),
		{
			DxrRetention.updateSessionState(userId) {
				copy(
					refreshTime = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { hole ->
		HoleCard(hole)
	}
}

private suspend fun ProducerScope<OtHole>.sendForumApiHoles(refreshTime: OffsetDateTime) {
	var startTime = refreshTime
	val loadLength = 10
	val sortOrder = DxrSettings.Models.sortOrderOrDefault

	while (true) {
		DxrForumApi.ensureAuth()
		val holes = DxrForumApi.loadHoles(
			startTime,
			null,
			length = loadLength.toLong(),
			sortOrder = sortOrder,
		)
		holes.forEach { hole ->
			send(hole)
		}
		holes.size < loadLength && break
		startTime = holes.last().getSortingDateTime(sortOrder)
	}
}

@Composable
private fun ForumApiFloorsPager(userId: Long, holeId: Long) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val holeSessionStateNullable by produceState<DxrHoleSessionState?>(null, userId, holeId) {
		val holeSessionStatePath = context.holeSessionStatePathOf(userId, holeId)
		updateWith(listOf(holeSessionStatePath.toFile())) {
			DxrRetention.loadHoleSessionState(userId, holeId)
		}
	}
	val holeSessionState = holeSessionStateNullable ?: return
	val refreshTime = holeSessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	ChannelPager(
		{
			runCatchingOnSnackbar(snackbarController) {
				if (holeSessionState.reversed == true) {
					sendForumApiFloorsReversed(holeId)
				} else {
					sendForumApiFloors(holeId)
				}
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "ForumApiFloorsPager")
			put("userId", userId)
			put("holeId", holeId)
			put("refreshTime", refreshTime.toStringRfc3339())
		}),
		16,
		holeSessionState.pagerFloorIndex ?: 0,
		holeSessionState.pagerFloorScrollOffset ?: 0,
		getFloorPositionSaver(userId, holeId),
		{
			DxrRetention.updateHoleSessionState(userId, holeId) {
				copy(
					refreshTime = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { (floor, hole, index) ->
		FloorCard(floor, hole, index)
	}
}

private suspend fun ProducerScope<Triple<OtFloor, OtHole, Int>>.sendForumApiFloors(holeId: Long) {
	DxrForumApi.ensureAuth()
	val hole = DxrForumApi.loadHoleById(holeId)

	var startFloorIndex = 0
	val loadLength = 50

	do {
		DxrForumApi.ensureAuth()
		val floors = DxrForumApi.loadFloors(
			hole,
			startFloor = startFloorIndex.toLong(),
			length = loadLength.toLong(),
		)
		floors.forEachIndexed { index, floor ->
			send(Triple(floor, hole, startFloorIndex + index))
		}
		startFloorIndex += floors.size
	} while (floors.size >= loadLength)
}

private suspend fun ProducerScope<Triple<OtFloor, OtHole, Int>>.sendForumApiFloorsReversed(holeId: Long) {
	DxrForumApi.ensureAuth()
	val hole = DxrForumApi.loadHoleById(holeId)

	var endFloorIndex = hole.floorsCount.toInt()
	val loadLength = 50

	do {
		DxrForumApi.ensureAuth()
		val startFloorIndex = (endFloorIndex - loadLength).coerceAtLeast(0)
		val floors = DxrForumApi.loadFloors(
			hole,
			startFloor = startFloorIndex.toLong(),
			length = (endFloorIndex - startFloorIndex).toLong(),
		).asReversed()
		floors.forEachIndexed { index, floor ->
			send(Triple(floor, hole, endFloorIndex - index - 1))
		}
		endFloorIndex = startFloorIndex
	} while (endFloorIndex > 0)
}

@Composable
private fun RetentionHolesPager(userId: Long) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current

	val refreshTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	val holeIndicesPath = remember(userId) { context.holeIndicesPathOf(userId) }
	// MT is short for Modified Time
	val holeIndicesMtString by produceState(holeIndicesPath.getLastModifiedTime().toString(), userId) {
		updateWith(listOf(holeIndicesPath.toFile())) {
			holeIndicesPath.getLastModifiedTime().toString()
		}
	}

	ChannelPager(
		{
			runCatchingOnSnackbar(snackbarController) {
				val holeSequence = DxrRetention.loadHoleSequenceByUpdate(userId)
				holeSequence.forEach { hole -> send(hole) }
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "RetentionHolesPager")
			put("userId", userId)
			put("refreshTime", refreshTime.toStringRfc3339())
			put("holeIndicesModifiedTime", holeIndicesMtString)
		}),
		16,
		sessionState.pagerHoleIndex ?: 0,
		sessionState.pagerHoleScrollOffset ?: 0,
		getHolePositionSaver(userId),
		{
			runCatchingOnSnackbar(snackbarController) {
				DxrRetention.storeForExample()
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { hole ->
		HoleCard(hole)
	}
}

@Composable
private fun RetentionFloorsPager(userId: Long, holeId: Long) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val holeSessionStateNullable by produceState<DxrHoleSessionState?>(null, userId, holeId) {
		val holeSessionStatePath = context.holeSessionStatePathOf(userId, holeId)
		updateWith(listOf(holeSessionStatePath.toFile())) {
			DxrRetention.loadHoleSessionState(userId, holeId)
		}
	}
	val holeSessionState = holeSessionStateNullable ?: return
	val refreshTime = holeSessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	val floorIndicesPath = remember(userId) { context.floorIndicesPathOf(userId) }
	// Mt is short for Modified Time
	val floorIndicesMtString by produceState(floorIndicesPath.getLastModifiedTime().toString(), userId, holeId) {
		updateWith(listOf(floorIndicesPath.toFile())) {
			floorIndicesPath.getLastModifiedTime().toString()
		}
	}

	ChannelPager(
		{
			// TODO reversed floors, also for retention
			runCatchingOnSnackbar(snackbarController) {
				val hole = checkNotNull(DxrRetention.loadHole(userId, holeId)) { "DxrRetention.loadHole($userId, $holeId) failed" }

				if (holeSessionState.reversed == true) {
					DxrRetention.loadFloorSequenceReversed(userId, holeId)
				} else {
					DxrRetention.loadFloorSequence(userId, holeId)
				}
					.forEachIndexed { index, floor ->
						send(Triple(floor, hole, index))
					}
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "RetentionFloorsPager")
			put("userId", userId)
			put("holeId", holeId)
			put("refreshTime", refreshTime.toStringRfc3339())
			put("floorIndicesModifiedTime", floorIndicesMtString)
		}),
		holeSessionState.pagerFloorIndex ?: 0,
		holeSessionState.pagerFloorScrollOffset ?: 0,
		0,
		getFloorPositionSaver(userId, holeId),
		{
			runCatchingOnSnackbar(snackbarController) {
				DxrRetention.storeForExample()
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { (floor, hole, index) ->
		FloorCard(floor, hole, index)
	}
}

private fun getHolePositionSaver(userId: Long) = { holeIndex: Int, holeScrollOffset: Int ->
	DxrRetention.updateSessionState(userId) {
		copy(
			pagerHoleIndex = holeIndex,
			pagerHoleScrollOffset = holeScrollOffset,
		)
	}
}

private fun getFloorPositionSaver(userId: Long, holeId: Long) = { floorIndex: Int, floorScrollOffset: Int ->
	DxrRetention.updateHoleSessionState(userId, holeId) {
		copy(
			pagerFloorIndex = floorIndex,
			pagerFloorScrollOffset = floorScrollOffset,
		)
	}
}
