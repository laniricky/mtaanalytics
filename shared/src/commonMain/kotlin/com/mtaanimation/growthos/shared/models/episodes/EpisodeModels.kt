package com.mtaanimation.growthos.shared.models.episodes

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    val id: String,
    val userId: String,
    val season: Int,
    val episode: Int,
    val releaseDateEpochMillis: Long,
    val views: Long,
    val revenue: Double,
    val watchTimeHours: Double,
    val shares: Long,
    val comments: Long,
    val likes: Long
)

@Serializable
data class RecordEpisodeRequest(
    val season: Int,
    val episode: Int,
    val releaseDateEpochMillis: Long,
    val views: Long = 0,
    val revenue: Double = 0.0,
    val watchTimeHours: Double = 0.0,
    val shares: Long = 0,
    val comments: Long = 0,
    val likes: Long = 0
)
