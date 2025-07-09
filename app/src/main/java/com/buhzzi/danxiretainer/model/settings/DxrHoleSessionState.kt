package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrHoleSessionState(
	val pagerFloorIndex: Int? = null,
	val pagerFloorScrollOffset: Int? = null,
	val forumApiTimeOfFloors: String? = null,
) {
	val pagerFloorIndexNotNull get() = checkNotNull(pagerFloorIndex) { this }
	val pagerFloorScrollOffsetNotNull get() = checkNotNull(pagerFloorScrollOffset) { this }
	val forumApiTimeOfFloorsNotNull get() = checkNotNull(forumApiTimeOfFloors) { this }
}
