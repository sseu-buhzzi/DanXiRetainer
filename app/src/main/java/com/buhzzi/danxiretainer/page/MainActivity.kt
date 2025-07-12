package com.buhzzi.danxiretainer.page

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buhzzi.danxiretainer.page.forum.ForumPage
import com.buhzzi.danxiretainer.page.settings.SettingsAccountPage
import com.buhzzi.danxiretainer.page.settings.SettingsAccountUserProfilePage
import com.buhzzi.danxiretainer.page.settings.SettingsGeneralPage
import com.buhzzi.danxiretainer.page.settings.SettingsNetworkPage
import com.buhzzi.danxiretainer.repository.retention.DxrRetention
import com.buhzzi.danxiretainer.repository.settings.DxrSettings

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		DxrSettings.init(this)
		DxrRetention.init(this)

		// val a = dxrJson.decodeFromString<List<OtHole>>(TEST_JSON_STRING)
		println()

		setContent {
			DxrApp()
		}
	}
}

@Composable
fun DxrApp() {
	val context = LocalContext.current
	MaterialTheme(
		colorScheme = if (isSystemInDarkTheme()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				dynamicDarkColorScheme(context)
			} else {
				darkColorScheme()
			}
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				dynamicLightColorScheme(context)
			} else {
				lightColorScheme()
			}
		},
	) {
		DxrRouter()
	}
}

val LocalNavController = compositionLocalOf<NavController> {
	error("No NavController provided")
}

@Composable
fun DxrRouter() {
	val navController = rememberNavController()

	CompositionLocalProvider(LocalNavController provides navController) {
		NavHost(navController, DxrDestination.Forum.route) {
			DxrDestination.entries.forEach { destination ->
				composable(destination.route) {
					destination.content()
				}
			}
		}
	}
}

@Composable
fun DxrScaffoldWrapper(
	modifier: Modifier = Modifier,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	floatingActionButtonPosition: FabPosition = FabPosition.End,
	containerColor: Color = MaterialTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
	content: @Composable (PaddingValues) -> Unit,
) {
	val snackbarHostState = remember { SnackbarHostState() }
	val snackbarController = remember(snackbarHostState) { SnackbarController(snackbarHostState) }

	CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
		Scaffold(
			modifier = modifier
				.safeDrawingPadding(),
			topBar = topBar,
			bottomBar = bottomBar,
			snackbarHost = {
				SnackbarHost(snackbarHostState)
			},
			floatingActionButton = floatingActionButton,
			floatingActionButtonPosition = floatingActionButtonPosition,
			contentColor = contentColor,
			contentWindowInsets = contentWindowInsets,
			content = content,
		)
	}
}

enum class DxrDestination(
	val route: String,
	val content: @Composable () -> Unit
) {
	Forum("/forum", {
		ForumPage()
	}),
	SettingsAccount("/settings/account", {
		SettingsAccountPage()
	}),
	SettingsAccountUserProfile("/settings/account/user-profile", {
		SettingsAccountUserProfilePage()
	}),
	SettingsNetwork("/settings/network", {
		SettingsNetworkPage()
	}),
	SettingsGeneral("/settings/general", {
		SettingsGeneralPage()
	});

	object ForumHoleArguments {
		const val HOLE_ID = "holeId"
	}
}
