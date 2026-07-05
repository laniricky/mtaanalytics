package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.DashboardApiService
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain-layer repository for dashboard data.
 * Isolates the ViewModel from knowing about Ktor or DataStore directly.
 */
@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: DashboardApiService
) {
    suspend fun getDashboard(deadlineOverrideEpochMillis: Long? = null): Result<DashboardProjection> =
        apiService.getDashboardProjection(deadlineOverrideEpochMillis)

    /** Signal that the cached dashboard is stale and should be re-fetched on next load. */
    fun invalidateCache() {
        // Currently no local cache — dashboard is always fetched fresh from the API.
        // This method exists so ViewModels can call it after writing new stats, ready
        // for future caching (e.g. Room or DataStore) without changing call sites.
    }
}

