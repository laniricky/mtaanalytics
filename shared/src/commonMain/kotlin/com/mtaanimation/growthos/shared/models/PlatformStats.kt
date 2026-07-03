package com.mtaanimation.growthos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class PlatformStats(
    val id: String,
    val platformType: PlatformType,
    val currentFollowers: Long,
    val target2036: Long,
    val dateRecordedEpochMillis: Long
)
