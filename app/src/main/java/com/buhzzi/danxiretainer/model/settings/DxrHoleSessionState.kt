package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrHoleSessionState(
	val reversed: Boolean? = false,
	val pagerFloorIndex: Int? = null,
	val pagerFloorScrollOffset: Int? = null,
	val refreshTime: String? = null,
) {
	val reversedNotNull get() = checkNotNull(reversed) { this }
	val pagerFloorIndexNotNull get() = checkNotNull(pagerFloorIndex) { this }
	val pagerFloorScrollOffsetNotNull get() = checkNotNull(pagerFloorScrollOffset) { this }
	val refreshTimeNotNull get() = checkNotNull(refreshTime) { this }
}
