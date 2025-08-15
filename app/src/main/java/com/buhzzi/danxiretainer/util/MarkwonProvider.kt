package com.buhzzi.danxiretainer.util

import android.content.Context
import coil.ImageLoader
import io.noties.markwon.Markwon
import io.noties.markwon.image.coil.CoilImagesPlugin

object MarkwonProvider {
	private lateinit var markwon: Markwon

	operator fun invoke(context: Context): Markwon {
		if (!::markwon.isInitialized) {
			markwon = Markwon.builder(context.applicationContext)
				.usePlugin(
					CoilImagesPlugin.create(
						context,
						ImageLoader.Builder(context)
							.components {
								// add(DxrForumApi.client)
							}
							.build(),
					)
				)
				.build()
		}
		return markwon
	}
}
