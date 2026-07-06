package com.mtaanimation.growthos.shared.models.customgoals

import kotlinx.serialization.Serializable

@Serializable
data class CustomGoalDto(
    val id: String,
    val userId: String,
    val title: String, // e.g., "1 Billion Views"
    val type: String, // e.g., "VIEWS", "REVENUE", "EPISODES"
    val targetValue: Double,
    val currentValue: Double,
    val deadlineEpochMillis: Long
)

@Serializable
data class CreateCustomGoalRequest(
    val title: String,
    val type: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val deadlineEpochMillis: Long
)

@Serializable
data class UpdateCustomGoalProgressRequest(
    val id: String,
    val currentValue: Double
)

/**
 * Live aggregate values computed from existing data sources.
 * Returned by GET /api/milestones/live-values
 */
@Serializable
data class MilestoneLiveValues(
    val totalViews: Long,       // sum of all episode_links.view_count
    val totalEpisodes: Long,    // count of episodes rows
    val totalRevenue: Double,   // sum of all revenue entries
    val totalFollowers: Long    // sum of latest currentFollowers across all platforms
)
