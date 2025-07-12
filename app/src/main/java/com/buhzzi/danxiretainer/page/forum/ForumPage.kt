package com.buhzzi.danxiretainer.page.forum

import android.os.FileObserver
import android.util.Log
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.derivedStateOf
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
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathStringFlow
import com.buhzzi.danxiretainer.repository.settings.contentSourceFlow
import com.buhzzi.danxiretainer.repository.settings.sortOrder
import com.buhzzi.danxiretainer.repository.settings.userProfile
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.floorIndicesPathOf
import com.buhzzi.danxiretainer.util.holeIndicesPathOf
import com.buhzzi.danxiretainer.util.sessionStateCurrentPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import dart.package0.dan_xi.model.forum.OtFloor
import dart.package0.dan_xi.model.forum.OtHole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import kotlin.io.path.Path
import kotlin.io.path.getLastModifiedTime

val LocalSessionState = compositionLocalOf<DxrSessionState?> { null }

@Composable
fun ForumPage() {
	val context = LocalContext.current

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)

	// TODO 移除其他trigger
	val sessionState by produceState<DxrSessionState?>(null, userProfile?.userId) {
		val userId = userProfile?.userId ?: return@produceState

		value = DxrRetention.loadSessionState(userId)

		val observer = userProfile?.userId?.let { userId ->
			object : FileObserver(context.sessionStateCurrentPathOf(userId).toFile(), CLOSE_WRITE) {
				override fun onEvent(event: Int, path: String?) {
					if (event == CLOSE_WRITE) {
						value = DxrRetention.loadSessionState(userId)
					}
				}
			}
		}
		observer?.startWatching()

		awaitDispose {
			observer?.stopWatching()
		}
	}

	var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
	val selectedPage by remember { derivedStateOf { ForumPages.entries[selectedIndex] } }

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ForumPageTopBar() {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current

	val scope = rememberCoroutineScope()

	TopAppBar(
		{
			sessionState?.holeId?.let { holeId ->
				Text("#$holeId")
			} ?: Text(stringResource(R.string.forum_label))
		},
		navigationIcon = {
			if (sessionState?.holeId != null) {
				fun goBackToForumHolesPage() {
					scope.launch(Dispatchers.IO) {
						runCatchingOnSnackbar(snackbarController) {
							val userId = checkNotNull(DxrSettings.Models.userProfile) { "No user profile" }.userIdNotNull
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

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)
	val contentSource by DxrSettings.Models.contentSourceFlow.collectAsState(null)

	Box(
		modifier = modifier,
	) {
		val backgroundImagePathString by DxrSettings.Items.backgroundImagePathStringFlow.collectAsState(null)
		backgroundImagePathString?.let { Path(it).toFile() }?.let { backgroundImageFile ->
			Image(
				rememberAsyncImagePainter(backgroundImageFile),
				null,
				modifier = Modifier
					.matchParentSize(),
				contentScale = ContentScale.Crop,
			)
		}
		userProfile?.userId?.let { userId ->
			when (contentSource) {
				DxrContentSource.FORUM_API -> sessionState?.holeId?.let { currentHoleId ->
					ForumApiFloorsPager(userId, currentHoleId)
				} ?: ForumApiHolesPager(userId)

				DxrContentSource.RETENTION -> sessionState?.holeId?.let { currentHoleId ->
					RetentionFloorsPager(userId, currentHoleId)
				} ?: RetentionHolesPager(userId)

				else -> Unit
			}
		}
	}
}

@Composable
private fun ForumApiHolesPager(userId: Long) {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current ?: return

	val forumApiRefreshTime = sessionState.forumApiRefreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	ChannelPager(
		{
			runCatchingOnSnackbar(snackbarController) {
				sendForumApiHoles(forumApiRefreshTime)
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "ForumApiHolesPager")
			put("userId", userId)
			put("timeKey", forumApiRefreshTime.toStringRfc3339())
		}),
		16,
		sessionState.pagerHoleIndex ?: 0,
		sessionState.pagerHoleScrollOffset ?: 0,
		{ holeIndex, holeScrollOffset ->
			DxrRetention.updateSessionState(userId) {
				copy(
					pagerHoleIndex = holeIndex,
					pagerHoleScrollOffset = holeScrollOffset,
				)
			}
		},
		{
			DxrRetention.updateSessionState(userId) {
				copy(
					forumApiRefreshTime = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { hole ->
		HoleCard(hole)
	}
}

private suspend fun ProducerScope<OtHole>.sendForumApiHoles(forumApiRefreshTime: OffsetDateTime) {
	var startTime = forumApiRefreshTime
	val loadLength = 10
	val sortOrder = DxrSettings.Models.sortOrder ?: return

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
	val snackbarController = LocalSnackbarController.current
	val holeSessionState = DxrRetention.loadHoleSessionState(userId, holeId)

	val forumApiRefreshTime = holeSessionState?.forumApiRefreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	ChannelPager(
		{
			// TODO reversed floors, also for retention
			runCatchingOnSnackbar(snackbarController) {
				if (holeSessionState?.reversed == true) {
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
			put("timeKey", forumApiRefreshTime.toStringRfc3339())
		}),
		16,
		holeSessionState?.pagerFloorIndex ?: 0,
		holeSessionState?.pagerFloorScrollOffset ?: 0,
		{ floorIndex, floorScrollOffset ->
			DxrRetention.updateHoleSessionState(userId, holeId) {
				copy(
					pagerFloorIndex = floorIndex,
					pagerFloorScrollOffset = floorScrollOffset,
				)
			}
		},
		{
			DxrRetention.updateHoleSessionState(userId, holeId) {
				copy(
					forumApiRefreshTime = OffsetDateTime.now().toStringRfc3339(),
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
	Log.d("sendForumApiFloorsReversed", "loadHoleById($holeId)")

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

	val holeIndicesPath by remember {
		derivedStateOf {
			context.holeIndicesPathOf(userId)
		}
	}
	// MT is short for Modified Time
	val holeIndicesMtString by produceState(holeIndicesPath.getLastModifiedTime().toString(), userId) {
		val observer = object : FileObserver(holeIndicesPath.toFile(), CLOSE_WRITE) {
			override fun onEvent(event: Int, path: String?) {
				if (event == CLOSE_WRITE) {
					value = holeIndicesPath.getLastModifiedTime().toString()
				}
			}
		}
		observer.startWatching()

		awaitDispose {
			observer.stopWatching()
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
			put("timeKey", holeIndicesMtString)
		}),
		16,
		0, // TODO
		0,
		{ holeIndex, holeScrollOffset ->
			// TODO
		},
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

	val floorIndicesPath by remember {
		derivedStateOf {
			context.floorIndicesPathOf(userId)
		}
	}
	// Mt is short for Modified Time
	val floorIndicesMtString by produceState(floorIndicesPath.getLastModifiedTime().toString(), userId, holeId) {
		val observer = object : FileObserver(floorIndicesPath.toFile(), CLOSE_WRITE) {
			override fun onEvent(event: Int, path: String?) {
				if (event == CLOSE_WRITE) {
					value = floorIndicesPath.getLastModifiedTime().toString()
				}
			}
		}
		observer.startWatching()

		awaitDispose {
			observer.stopWatching()
		}
	}

	ChannelPager(
		{
			// TODO reversed floors, also for retention
			runCatchingOnSnackbar(snackbarController) {
				val hole = requireNotNull(DxrRetention.loadHole(userId, holeId)) { "DxrRetention.loadHole($userId, $holeId) failed" }

				DxrRetention.loadFloorSequence(userId, holeId)
					.forEachIndexed { index, floor ->
						send(Triple(floor, hole, index))
					}
			}
		},
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "RetentionFloorsPager")
			put("userId", userId)
			put("holeId", holeId)
			put("timeKey", floorIndicesMtString)
		}),
		16,
		1820, // TODO
		0,
		{ floorIndex, floorScrollOffset ->
			// TODO
		},
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
