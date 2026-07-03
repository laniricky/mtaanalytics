package com.mtaanimation.growthos.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: String,
    val name: String,
    val targetValue: Long,
    val currentProgress: Long,
    val targetDateEpochMillis: Long,
    val type: GoalType
)

@Serializable
enum class GoalType {
    FOLLOWERS,
    VIEWS,
    EPISODES,
    REVENUE,
    OTHER
}
