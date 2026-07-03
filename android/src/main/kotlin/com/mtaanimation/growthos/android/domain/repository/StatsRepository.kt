package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.StatsApiService
import com.mtaanimation.growthos.shared.models.PlatformStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val apiService: StatsApiService
) {
    suspend fun getAllStats(): Result<List<PlatformStats>> = apiService.getAllStats()

    suspend fun recordStats(request: com.mtaanimation.growthos.shared.models.RecordStatsRequest): Result<PlatformStats> =
        apiService.postStats(request)
}
