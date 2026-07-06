package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.EpisodeApiService
import com.mtaanimation.growthos.shared.models.episodes.CreateEpisodeRequest
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import com.mtaanimation.growthos.shared.models.episodes.EpisodeLinkDto
import com.mtaanimation.growthos.shared.models.episodes.UpsertEpisodeLinkRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeRepository @Inject constructor(
    private val apiService: EpisodeApiService
) {
    suspend fun getEpisodes(): Result<List<EpisodeDto>> = apiService.getEpisodes()

    suspend fun createEpisode(request: CreateEpisodeRequest): Result<EpisodeDto> =
        apiService.createEpisode(request)

    suspend fun upsertLink(episodeId: String, request: UpsertEpisodeLinkRequest): Result<EpisodeLinkDto> =
        apiService.upsertLink(episodeId, request)
        
    suspend fun deleteEpisode(episodeId: String): Result<Unit> =
        apiService.deleteEpisode(episodeId)
}
