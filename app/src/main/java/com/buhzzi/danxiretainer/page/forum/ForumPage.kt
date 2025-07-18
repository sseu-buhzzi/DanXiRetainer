package com.buhzzi.danxiretainer.page.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.LocalSnackbarController
import com.buhzzi.danxiretainer.page.runCatchingOnSnackbar
import com.buhzzi.danxiretainer.page.showExceptionOnSnackbar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathFlow
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefaultFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.holesSessionStatePathOf
import com.buhzzi.danxiretainer.util.sessionStateDirPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import com.buhzzi.danxiretainer.util.updateWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime

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
			sessionState.holeId ?: return@TopAppBar
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
		},
		actions = {
			val holeId = sessionState.holeId ?: run {
				HolesTopBarActions()
				return@TopAppBar
			}
			FloorsTopBarActions(holeId)
		}
	)
}

@Composable
fun ForumPageContent(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier,
	) {
		val backgroundImagePath by DxrSettings.Models.backgroundImagePathFlow.collectAsState(null)
		backgroundImagePath?.toFile()?.let { backgroundImageFile ->
			Image(
				rememberAsyncImagePainter(backgroundImageFile),
				null,
				modifier = Modifier
					.matchParentSize(),
				contentScale = ContentScale.Crop,
			)
		}

		val sessionState = LocalSessionState.current

		val userProfileNullable by DxrSettings.Models.userProfileFlow.collectAsState(null)
		val userProfile = userProfileNullable ?: return
		val userId = userProfile.userId ?: return

		val holeId = sessionState.holeId ?: run {
			HolesPager(userId)
			return
		}
		FloorsPager(userId, holeId)
	}
}

@Composable
private fun HolesPager(userId: Long) {
	val snackbarController = LocalSnackbarController.current
	val sessionState = LocalSessionState.current
	val refreshTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	val contentSource by DxrSettings.Models.contentSourceOrDefaultFlow.collectAsState(
		DxrSettings.Models.contentSourceOrDefault,
	)

	ChannelPager(
		DxrContent.holesFlow()
			.catch { exception -> showExceptionOnSnackbar(snackbarController, exception) },
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "HolesPager")
			put("contentSource", contentSource.name)
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

@Composable
private fun FloorsPager(userId: Long, holeId: Long) {
	val context = LocalContext.current
	val snackbarController = LocalSnackbarController.current

	val holeSessionStateNullable by produceState<DxrHoleSessionState?>(null, userId, holeId) {
		val holeSessionStatePath = context.holesSessionStatePathOf(userId, holeId)
		updateWith(listOf(holeSessionStatePath.toFile())) {
			DxrRetention.loadHoleSessionState(userId, holeId)
		}
	}
	val holeSessionState = holeSessionStateNullable ?: return
	val refreshTime = holeSessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()

	val contentSource by DxrSettings.Models.contentSourceOrDefaultFlow.collectAsState(
		DxrSettings.Models.contentSourceOrDefault,
	)

	ChannelPager(
		DxrContent.floorsFlow(holeId)
			.catch { exception -> showExceptionOnSnackbar(snackbarController, exception) },
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "FloorsPager")
			put("contentSource", contentSource.name)
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
