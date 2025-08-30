package com.buhzzi.danxiretainer.page

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SnackbarProvider(
	private val snackbarHostState: SnackbarHostState,
) {
	private val mainScope = CoroutineScope(Dispatchers.Main)

	operator fun invoke(block: suspend SnackbarHostState.() -> Unit) {
		mainScope.launch {
			snackbarHostState.block()
		}
	}

	/**
	 * Time consuming operations are always with the need to show exceptions, so we provide this method in convenience of using
	 * [withContext], [runCatching], [showException] at the same time.
 	 */
	suspend fun <R> runShowingWithContext(
		context: CoroutineContext,
		lazyMessage: (Throwable) -> String = { it.toString() },
		block: suspend () -> R,
	) = withContext(context) {
		runShowingSuspend(lazyMessage, block)
	}

	suspend fun <R> runShowingSuspend(
		lazyMessage: (Throwable) -> String = { it.toString() },
		block: suspend () -> R,
	) = runCatching {
		block()
	}.onFailure { exception ->
		showException(exception, lazyMessage)
	}

	fun <R> runShowing(
		lazyMessage: (Throwable) -> String = { it.toString() },
		block: () -> R,
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
