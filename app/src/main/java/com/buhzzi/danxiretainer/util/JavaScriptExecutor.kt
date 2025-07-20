package com.buhzzi.danxiretainer.util

import android.app.Application
import android.content.Context
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox

object JavaScriptExecutor {
	private lateinit var app: Application

	private val sandbox by lazy {
		check(JavaScriptSandbox.isSupported()) { "JavaScriptSandbox is not supported on the system" }
		JavaScriptSandbox.createConnectedInstanceAsync(app).get()
	}

	private var isolate: JavaScriptIsolate? = null

	fun init(context: Context) {
		app = context.applicationContext as Application
	}

	fun execute(script: String, reuseIsolate: Boolean = true): String {
		val isolateNotNull = isolate
			?.takeIf { reuseIsolate }
			?: sandbox.createIsolate().also {
				isolate?.close()
				isolate = it
			}
		return isolateNotNull.evaluateJavaScriptAsync(script).get()
	}
}
