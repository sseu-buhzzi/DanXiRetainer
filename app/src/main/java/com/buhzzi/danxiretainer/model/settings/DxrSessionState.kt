package com.buhzzi.danxiretainer.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DxrSessionState(
	val holeId: Long? = null,
	val holeInitialFloorRank: Long? = null,
	val forumApiTimeOfHoles: String? = null,
	val forumApiTimeOfFloors: String? = null,
)
