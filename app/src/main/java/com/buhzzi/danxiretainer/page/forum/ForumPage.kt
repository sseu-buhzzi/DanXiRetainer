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
import com.buhzzi.danxiretainer.page.runBlockingOrShowSnackbarMessage
import com.buhzzi.danxiretainer.repository.api.forum.DxrForumApi
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathStringFlow
import com.buhzzi.danxiretainer.repository.settings.contentSourceFlow
import com.buhzzi.danxiretainer.repository.settings.sortOrder
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.util.floorIndicesPathOf
import com.buhzzi.danxiretainer.util.holeIndicesPathOf
import com.buhzzi.danxiretainer.util.sessionStateCurrentPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
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

	var selectedIndex by remember { mutableIntStateOf(0) }
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

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val scope = rememberCoroutineScope()

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)

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
						runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
							val userId = checkNotNull(userProfile?.userId)
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
					ForumApiFloorsPager(
						userId, currentHoleId,
						sessionState.holeInitialFloorRank?.toInt() ?: 0,
					)
				} ?: ForumApiHolesPager(userId)

				DxrContentSource.RETENTION -> sessionState?.holeId?.let { currentHoleId ->
					RetentionFloorsPager(
						userId, currentHoleId,
						sessionState.holeInitialFloorRank?.toInt() ?: 0,
					)
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

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val forumApiTimeOfHoles = sessionState.forumApiTimeOfHoles?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	VerticalBidirectionalChannelPager(
		{
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					// Load backward in a fixed time interval
					// TODO untested
					var endTime = forumApiTimeOfHoles
					var interval = Duration.ofMillis(256 * 1024)
					val loadLength = 10
					val sortOrder = DxrSettings.Models.sortOrder ?: return@runBlockingOrShowSnackbarMessage

					while (!endTime.isAfter(OffsetDateTime.now())) {
						var reachedEnd = false
						val nextEndTime = endTime.plus(interval)
						val holesInInterval = buildList {
							var startTime = nextEndTime
							while (true) {
								DxrForumApi.ensureAuth()
								val holes = DxrForumApi.loadHoles(
									startTime,
									null,
									length = loadLength.toLong(),
									sortOrder = sortOrder,
								).asReversed()
								reachedEnd = holes.size < loadLength
								reachedEnd && break

								val endingHoleIndex = holes.indexOfFirst { hole ->
									!hole.getSortingDateTime(sortOrder).isBefore(endTime)
								}
								if (endingHoleIndex != -1) {
									add(holes.subList(endingHoleIndex, holes.size))
								}
								endingHoleIndex != 0 && break

								startTime = holes.first().getSortingDateTime(sortOrder)
							}
						}.asReversed().flatten()
						holesInInterval.forEach { hole -> send(hole) }
						reachedEnd && break

						holesInInterval.lastOrNull()?.getSortingDateTime(sortOrder)
							?.let { Duration.between(endTime, it) }
							?.multipliedBy(loadLength.toLong())
							?.dividedBy(holesInInterval.size.toLong())
							?.let { interval = it }
						endTime = nextEndTime
					}
				}
			}
		},
		{
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					var startTime = forumApiTimeOfHoles
					val loadLength = 10
					val sortOrder = DxrSettings.Models.sortOrder ?: return@runBlockingOrShowSnackbarMessage

					while (true) {
						DxrForumApi.ensureAuth()
						val holes = DxrForumApi.loadHoles(
							startTime,
							null,
							length = loadLength.toLong(),
							sortOrder = sortOrder,
						)
						holes.forEach { hole -> send(hole) }
						holes.size < loadLength && break
						startTime = holes.last().getSortingDateTime(sortOrder)
					}
				}
			}
		},
		"forumApiHolesPager&userId=$userId&timeKey=$forumApiTimeOfHoles",
		16,
		{
			DxrRetention.updateSessionState(userId) {
				copy(
					forumApiTimeOfHoles = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { hole ->
		HoleCard(hole)
	}
}

@Composable
private fun ForumApiFloorsPager(userId: Long, holeId: Long, initialFloorRank: Int = 0) {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current ?: return

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val timeKey = sessionState.forumApiTimeOfHoles

	VerticalBidirectionalChannelPager(
		{
			// TODO scroll to forward items when backward items are added in
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					DxrForumApi.ensureAuth()
					val hole = DxrForumApi.loadHoleById(holeId)

					var endFloorRank = initialFloorRank.coerceIn(0 ..< hole.floorsCount.toInt())
					val loadLength = 50

					while (endFloorRank > 0) {
						DxrForumApi.ensureAuth()
						val startFloorRank = (endFloorRank - loadLength).coerceAtLeast(0)
						val floors = DxrForumApi.loadFloors(
							hole,
							startFloor = startFloorRank.toLong(),
							length = (endFloorRank - startFloorRank).toLong(),
						).asReversed()
						Log.d("ForumApiFloorsPager backward", "$startFloorRank\t${endFloorRank - startFloorRank}")
						floors.forEachIndexed { index, floor ->
							send(Triple(floor, hole, endFloorRank - index - 1))
						}
						endFloorRank -= floors.size
					}
				}
			}
		},
		{
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					DxrForumApi.ensureAuth()
					val hole = DxrForumApi.loadHoleById(holeId)

					var startFloorRank = initialFloorRank.coerceIn(0 ..< hole.floorsCount.toInt())
					val loadLength = 50

					do {
						DxrForumApi.ensureAuth()
						val floors = DxrForumApi.loadFloors(
							hole,
							startFloor = startFloorRank.toLong(),
							length = loadLength.toLong(),
						)
						floors.forEachIndexed { index, floor ->
							send(Triple(floor, hole, startFloorRank + index))
						}
						startFloorRank += floors.size
					} while (floors.size >= loadLength)
				}
			}
		},
		"forumApiFloorsPager&userId=$userId&holeId=$holeId&timeKey=$timeKey",
		16,
		{
			DxrRetention.updateSessionState(userId) {
				copy(
					forumApiTimeOfHoles = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { (floor, hole, rank) ->
		FloorCard(floor, hole, rank)
	}
}

@Composable
private fun RetentionHolesPager(userId: Long) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val holeIndicesPath by remember {
		derivedStateOf {
			context.holeIndicesPathOf(userId)
		}
	}
	val timeKey by produceState(holeIndicesPath.getLastModifiedTime().toString(), userId) {
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

	VerticalBidirectionalChannelPager(
		{
			// TODO 添加向前瀏覽
		},
		{
			val holeSequence = DxrRetention.loadHoleSequenceByUpdate(userId)
			holeSequence.forEach { hole -> send(hole) }
		},
		"retentionHolesPager&usersId=$userId&timeKey=$timeKey",
		16,
		{
			runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
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
private fun RetentionFloorsPager(userId: Long, holeId: Long, initialFloorRank: Int) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val unknownErrorLabel = stringResource(R.string.unknown_error_label)

	val floorIndicesPath by remember {
		derivedStateOf {
			context.floorIndicesPathOf(userId)
		}
	}
	val timeKey by produceState(floorIndicesPath.getLastModifiedTime().toString(), userId, holeId) {
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

	VerticalBidirectionalChannelPager(
		{
			// TODO untested
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					val hole = requireNotNull(DxrRetention.loadHole(userId, holeId))
					val floorsCount = hole.floorsCount.toInt()

					var endFloorRank = initialFloorRank.coerceIn(0 ..< floorsCount)
					DxrRetention.loadFloorSequenceReversed(userId, holeId)
						.drop(floorsCount - endFloorRank)
						.forEachIndexed { index, floor ->
							println("backward\t${endFloorRank - index - 1}\t${floor.content}")
							send(Triple(floor, hole, endFloorRank - index - 1))
						}
				}
			}
		},
		{
			launch(Dispatchers.IO) {
				runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
					val hole = requireNotNull(DxrRetention.loadHole(userId, holeId))

					var startFloorRank = initialFloorRank.coerceIn(0 ..< hole.floorsCount.toInt())
					DxrRetention.loadFloorSequence(userId, holeId)
						.drop(startFloorRank)
						.forEachIndexed { index, floor ->
							println("forward\t$index\t${floor.content}")
							send(Triple(floor, hole, index))
						}
				}
			}
		},
		"retentionFloorsPager&userId=$userId&holeId=$holeId&timeKey=$timeKey",
		16,
		{
			runBlockingOrShowSnackbarMessage(snackbarController, { it.message ?: unknownErrorLabel }) {
				DxrRetention.storeForExample()
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { (floor, hole, rank) ->
		FloorCard(floor, hole, rank)
	}
}
