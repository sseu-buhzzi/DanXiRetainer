package com.buhzzi.danxiretainer.page

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnackbarProvider(
	private val snackbarHostState: SnackbarHostState,
) {
	private val mainScope = CoroutineScope(Dispatchers.Main)

	operator fun invoke(block: suspend SnackbarHostState.() -> Unit) {
		mainScope.launch {
			snackbarHostState.block()
		}
	}

	suspend fun <R> runShowing(
		lazyMessage: (Throwable) -> String = { it.toString() },
		block: suspend () -> R,
	) = runCatching {
		block()
	}.onFailure { exception ->
		showException(exception, lazyMessage)
	}

	fun showException(
		exception: Throwable,
		lazyMessage: (Throwable) -> String = { it.toString() },
	) {
		Log.e("SnackbarProvider", "exception: $exception\nstacktrace:\n${exception.stackTraceToString()}")
		this { showSnackbar(lazyMessage(exception)) }
	}
}
