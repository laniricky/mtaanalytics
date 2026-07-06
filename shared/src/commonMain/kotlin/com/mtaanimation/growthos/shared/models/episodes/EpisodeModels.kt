package com.mtaanimation.growthos.shared.models.episodes

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    val id: String,
    val title: String,
    val description: String?,
    val publishedAt: Long,
    val totalViews: Long,
    val links: List<EpisodeLinkDto>
)

@Serializable
data class EpisodeLinkDto(
    val id: String,
    val platform: String,
    val url: String?,
    val viewCount: Long,
    val updatedAt: Long
)

@Serializable
data class CreateEpisodeRequest(
    val title: String,
    val description: String? = null,
    val publishedAt: Long
)

@Serializable
data class UpsertEpisodeLinkRequest(
    val platform: String,
    val url: String? = null,
    val viewCount: Long
)
