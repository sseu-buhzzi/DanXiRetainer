package com.buhzzi.danxiretainer.util

import android.os.FileObserver
import androidx.compose.runtime.ProduceStateScope
import java.io.File

suspend fun <T> ProduceStateScope<T>.updateWith(files: List<File>, updater: () -> T) {
	value = updater()

	val observer = object : FileObserver(files, CLOSE_WRITE) {
		override fun onEvent(event: Int, path: String?) {
			if (event == CLOSE_WRITE) {
				value = updater()
			}
		}
	}
	observer.startWatching()

	awaitDispose {
		observer.stopWatching()
	}
}
