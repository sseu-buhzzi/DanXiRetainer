package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrSessionState(
	val holeId: Long? = null,
	val pagerHoleIndex: Int? = null,
	val pagerHoleScrollOffset: Int? = null,
	val forumApiRefreshTime: String? = null,
) {
	val holeIdNotNull get() = checkNotNull(holeId) { this }
	val pagerHoleIndexNotNull get() = checkNotNull(pagerHoleIndex) { this }
	val pagerHoleScrollOffsetNotNull get() = checkNotNull(pagerHoleScrollOffset) { this }
	val forumApiRefreshTimeNotNull get() = checkNotNull(forumApiRefreshTime) { this }
}
