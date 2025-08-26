package com.buhzzi.danxiretainer.page.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.buhzzi.danxiretainer.R
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.DxrScaffoldWrapper
import com.buhzzi.danxiretainer.page.retension.RetentionPageContent
import com.buhzzi.danxiretainer.page.retension.RetentionPageTopBar
import com.buhzzi.danxiretainer.page.settings.SettingsPageContent
import com.buhzzi.danxiretainer.page.settings.SettingsPageTopBar
import com.buhzzi.danxiretainer.repository.content.DxrContent
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings
import com.buhzzi.danxiretainer.repository.settings.backgroundImagePathFlow
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefault
import com.buhzzi.danxiretainer.repository.settings.contentSourceOrDefaultFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileFlow
import com.buhzzi.danxiretainer.repository.settings.userProfileNotNull
import com.buhzzi.danxiretainer.util.LocalFilterContext
import com.buhzzi.danxiretainer.util.LocalHoleSessionState
import com.buhzzi.danxiretainer.util.LocalSessionState
import com.buhzzi.danxiretainer.util.LocalSnackbarProvider
import com.buhzzi.danxiretainer.util.dxrJson
import com.buhzzi.danxiretainer.util.holeSessionStatePathOf
import com.buhzzi.danxiretainer.util.sessionStateCurrentPathOf
import com.buhzzi.danxiretainer.util.toDateTimeRfc3339
import com.buhzzi.danxiretainer.util.toStringRfc3339
import com.buhzzi.danxiretainer.util.updateWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime

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
fun ForumPage() {
	val context = LocalContext.current

	val userProfile by DxrSettings.Models.userProfileFlow.collectAsState(null)
	// optional TODO this kind of `return`s can be more user-friendly loading information boxes
	val userId = userProfile?.userId ?: return

	val sessionStateNullable by produceState<DxrSessionState?>(null, userId) {
		val sessionStateCurrentPath = context.sessionStateCurrentPathOf(userId)
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
	val snackbarProvider = LocalSnackbarProvider.current
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
					snackbarProvider.runShowing {
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

		Column {
			val context = LocalContext.current
			val lifecycleOwner = LocalLifecycleOwner.current
			val sessionState = LocalSessionState.current

			val userProfileNullable by DxrSettings.Models.userProfileFlow.collectAsState(null)
			val userProfile = userProfileNullable ?: return
			val userId = userProfile.userId ?: return

			val holeId = sessionState.holeId ?: run {
				val holesFilterContextNullable by produceState<DxrHolesFilterContext?>(
					null,
					userId,
				) {
					value = DxrRetention.loadHolesFilterContext(userId)
				}
				DisposableEffect(userId, lifecycleOwner) {
					val observer = LifecycleEventObserver { _, event ->
						if (
							event == Lifecycle.Event.ON_PAUSE ||
							event == Lifecycle.Event.ON_STOP
						) {
							holesFilterContextNullable?.store()
						}
					}
					lifecycleOwner.lifecycle.addObserver(observer)
					onDispose {
						lifecycleOwner.lifecycle.removeObserver(observer)
						holesFilterContextNullable?.store()
					}
				}
				val holesFilterContext = holesFilterContextNullable ?: return

				CompositionLocalProvider(LocalFilterContext provides holesFilterContext) {
					FiltersColumn()
					HolesPager(userId)
				}
				return
			}

			val holeSessionStateNullable by produceState<DxrHoleSessionState?>(null, userId, holeId) {
				val sessionStateCurrentPath = context.holeSessionStatePathOf(userId, holeId)
				updateWith(listOf(sessionStateCurrentPath.toFile())) {
					DxrRetention.loadHoleSessionState(userId, holeId)
				}
			}
			val holeSessionState = holeSessionStateNullable ?: return

			val floorsFilterContextNullable by produceState<DxrFloorsFilterContext?>(
				null,
				userId, holeId,
			) {
				value = DxrRetention.loadFloorsFilterContext(userId, holeId)
			}
			DisposableEffect(userId, holeId, lifecycleOwner) {
				val observer = LifecycleEventObserver { _, event ->
					if (
						event == Lifecycle.Event.ON_PAUSE ||
						event == Lifecycle.Event.ON_STOP
					) {
						floorsFilterContextNullable?.store()
					}
				}
				lifecycleOwner.lifecycle.addObserver(observer)
				onDispose {
					lifecycleOwner.lifecycle.removeObserver(observer)
					floorsFilterContextNullable?.store()
				}
			}
			val floorsFilterContext = floorsFilterContextNullable ?: return

			CompositionLocalProvider(
				LocalHoleSessionState provides holeSessionState,
				LocalFilterContext provides floorsFilterContext,
			) {
				FiltersColumn()
				FloorsPager(userId)
			}
		}
	}
}

@Composable
private fun HolesPager(userId: Long) {
	val snackbarProvider = LocalSnackbarProvider.current
	val sessionState = LocalSessionState.current
	val refreshTime = sessionState.refreshTime?.toDateTimeRfc3339() ?: OffsetDateTime.now()
	val holesFilterContext = LocalFilterContext.current as DxrHolesFilterContext

	val contentSource by DxrSettings.Models.contentSourceOrDefaultFlow.collectAsState(
		DxrSettings.Models.contentSourceOrDefault,
	)

	ChannelPager(
		DxrContent.holesFlow(holesFilterContext)
			.catch { exception -> snackbarProvider.showException(exception) },
		dxrJson.encodeToString(buildJsonObject {
			put("fun", "HolesPager")
			put("contentSource", contentSource.name)
			put("userId", userId)
			put("refreshTime", refreshTime.toStringRfc3339())
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
private fun FloorsPager(userId: Long) {
	val context = LocalContext.current
	val sessionState = LocalSessionState.current
	val snackbarProvider = LocalSnackbarProvider.current
	val floorsFilterContext = LocalFilterContext.current as DxrFloorsFilterContext

	val holeId = sessionState.holeId ?: return
	val holeSessionStateNullable by produceState<DxrHoleSessionState?>(null, userId, holeId) {
		val holeSessionStatePath = context.holeSessionStatePathOf(userId, holeId)
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
		DxrContent.floorsFlow(holeId, floorsFilterContext)
			.catch { exception -> snackbarProvider.showException(exception) },
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
					refreshTime = OffsetDateTime.now().toStringRfc3339(),
				)
			}
		},
		modifier = Modifier
			.fillMaxSize(),
	) { locatedFloor ->
		FloorCard(locatedFloor)
	}
}
