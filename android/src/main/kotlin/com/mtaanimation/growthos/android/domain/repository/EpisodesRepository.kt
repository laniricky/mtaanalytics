package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.EpisodesApiService
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import com.mtaanimation.growthos.shared.models.episodes.RecordEpisodeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodesRepository @Inject constructor(
    private val apiService: EpisodesApiService
) {
    suspend fun getAllEpisodes(): Result<List<EpisodeDto>> = apiService.getAllEpisodes()

    suspend fun recordEpisode(request: RecordEpisodeRequest): Result<EpisodeDto> =
        apiService.postEpisode(request)
}
