package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrSessionState(
	val holeId: Long? = null,
	val forumApiTimeOfHoles: String? = null,
	val forumApiTimeOfFloors: String? = null,
) {
	val holeIdNotNull get() = checkNotNull(holeId) { this }
	val forumApiTimeOfHolesNotNull get() = checkNotNull(forumApiTimeOfHoles) { this }
	val forumApiTimeOfFloorsNotNull get() = checkNotNull(forumApiTimeOfFloors) { this }
}
