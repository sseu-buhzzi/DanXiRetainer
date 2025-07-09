package com.buhzzi.danxiretainer.page

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SnackbarController(
	private val snackbarHostState: SnackbarHostState,
) {
	private val mainScope = CoroutineScope(Dispatchers.Main)

	fun show(
		message: String,
		actionLabel: String? = null,
		withDismissAction: Boolean = false,
		duration: SnackbarDuration = if (actionLabel == null) {
			SnackbarDuration.Short
		} else {
			SnackbarDuration.Indefinite
		}
	) {
		mainScope.launch {
			snackbarHostState.showSnackbar(
				message,
				actionLabel,
				withDismissAction,
				duration,
			)
		}
	}

	fun show(visuals: SnackbarVisuals) {
		mainScope.launch {
			snackbarHostState.showSnackbar(visuals)
		}
	}
}

val LocalSnackbarController = compositionLocalOf<SnackbarController> {
	error("SnackbarController not provided")
}

fun CoroutineScope.runBlockingOrShowSnackbarMessage(
	snackbarController: SnackbarController,
	lazyMessage: (Throwable) -> String,
	context: CoroutineContext = EmptyCoroutineContext,
	block: suspend CoroutineScope.() -> Unit,
) = runBlocking(context) {
	runCatching {
		block()
	}.getOrElse { exception ->
		Log.e("runBlockingOrShowSnackbarMessage", "exception: $exception\nstacktrace:\n${exception.stackTraceToString()}")
		snackbarController.show(lazyMessage(exception))
	}
}
