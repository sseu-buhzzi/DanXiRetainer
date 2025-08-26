package com.buhzzi.danxiretainer.util

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import com.buhzzi.danxiretainer.model.forum.DxrFilterContext
import com.buhzzi.danxiretainer.model.settings.DxrHoleSessionState
import com.buhzzi.danxiretainer.model.settings.DxrSessionState
import com.buhzzi.danxiretainer.page.SnackbarProvider

val LocalNavController = compositionLocalOf<NavController> {
	error("No NavController provided")
}

val LocalSnackbarProvider = compositionLocalOf<SnackbarProvider> {
	error("SnackbarProvider not provided")
}

val LocalSessionState = compositionLocalOf<DxrSessionState> {
	error("LocalSessionState not provided")
}

val LocalHoleSessionState = compositionLocalOf<DxrHoleSessionState> {
	error("LocalHoleSessionState not provided")
}

val LocalFilterContext = compositionLocalOf<DxrFilterContext> {
	error("LocalFilter not provided")
}
