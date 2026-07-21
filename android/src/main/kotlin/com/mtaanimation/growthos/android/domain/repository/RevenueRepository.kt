package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.RevenueApiService
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import com.mtaanimation.growthos.shared.projection.RevenueProjection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueRepository @Inject constructor(
    private val apiService: RevenueApiService
) {
    suspend fun getAllRevenue(): Result<List<RevenueEntryDto>> = apiService.getAllRevenue()

    suspend fun recordRevenue(request: RecordRevenueRequest): Result<RevenueEntryDto> =
        apiService.postRevenue(request)

    suspend fun getRevenueProjection(deadlineEpochMillis: Long? = null): Result<RevenueProjection> =
        apiService.getRevenueProjection(deadlineEpochMillis)
}
