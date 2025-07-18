package com.buhzzi.danxiretainer.util

import android.annotation.SuppressLint
import com.squareup.duktape.Duktape

@SuppressLint("SetJavaScriptEnabled")
object JavaScriptExecutor {
	private val duktape by lazy { Duktape.create() }

	fun execute(script: String) = duktape.evaluate(script)
}
