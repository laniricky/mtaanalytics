package com.mtaanimation.growthos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateGoalRequest(
    val name: String,
    val targetValue: Long,
    val targetDateEpochMillis: Long,
    val type: GoalType
)

@Serializable
data class RecordStatsRequest(
    val platformType: PlatformType,
    val currentFollowers: Long,
    val dateRecordedEpochMillis: Long
)
