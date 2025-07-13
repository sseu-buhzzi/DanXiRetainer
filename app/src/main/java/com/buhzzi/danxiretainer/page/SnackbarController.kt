package com.buhzzi.danxiretainer.page

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
		},
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

suspend fun <R> runCatchingOnSnackbar(
	snackbarController: SnackbarController,
	lazyMessage: (Throwable) -> String = { it.toString() },
	block: suspend () -> R,
) = Unit.runCatchingOnSnackbar(
	snackbarController = snackbarController,
	lazyMessage = lazyMessage,
) {
	block()
}

suspend fun <T, R> T.runCatchingOnSnackbar(
	snackbarController: SnackbarController,
	lazyMessage: (Throwable) -> String = { it.toString() },
	block: suspend T.() -> R,
) = runCatching {
	block()
}.onFailure { exception ->
	showExceptionOnSnackbar(snackbarController, exception, lazyMessage)
}

fun showExceptionOnSnackbar(
	snackbarController: SnackbarController,
	exception: Throwable,
	lazyMessage: (Throwable) -> String = { it.toString() },
) {
	Log.e("SnackbarController", "exception: $exception\nstacktrace:\n${exception.stackTraceToString()}")
	snackbarController.show(lazyMessage(exception))
}
