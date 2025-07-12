package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrHoleSessionState(
	val reversed: Boolean? = false,
	val pagerFloorIndex: Int? = null,
	val pagerFloorScrollOffset: Int? = null,
	val forumApiRefreshTime: String? = null,
) {
	val reversedNotNull get() = checkNotNull(reversed) { this }
	val pagerFloorIndexNotNull get() = checkNotNull(pagerFloorIndex) { this }
	val pagerFloorScrollOffsetNotNull get() = checkNotNull(pagerFloorScrollOffset) { this }
	val forumApiRefreshTimeNotNull get() = checkNotNull(forumApiRefreshTime) { this }
}
