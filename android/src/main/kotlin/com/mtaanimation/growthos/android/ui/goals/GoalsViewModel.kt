package com.mtaanimation.growthos.android.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.DashboardRepository
import com.mtaanimation.growthos.android.domain.repository.StatsRepository
import com.mtaanimation.growthos.shared.models.RecordStatsRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackingSubmitState {
    data object Idle : TrackingSubmitState
    data object Submitting : TrackingSubmitState
    data object Success : TrackingSubmitState
    data class Error(val message: String) : TrackingSubmitState
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<TrackingSubmitState>(TrackingSubmitState.Idle)
    val submitState: StateFlow<TrackingSubmitState> = _submitState.asStateFlow()

    private val _dashboardState = MutableStateFlow<com.mtaanimation.growthos.shared.projection.DashboardProjection?>(null)
    val dashboardState: StateFlow<com.mtaanimation.growthos.shared.projection.DashboardProjection?> = _dashboardState.asStateFlow()

    init {
        fetchDashboard()
    }

    private fun fetchDashboard() {
        viewModelScope.launch {
            val result = dashboardRepository.getDashboard()
            if (result.isSuccess) {
                _dashboardState.value = result.getOrNull()
            }
        }
    }

    fun recordBatchStats(
        statsMap: Map<String, Long>,
        dateEpochMillis: Long
    ) {
        viewModelScope.launch {
            _submitState.value = TrackingSubmitState.Submitting
            
            var hasError = false
            var errorMessage = ""

            // Launch sequentially or concurrently. Here we'll just await all sequentially for simplicity and safety.
            for ((platformStr, currentFollowers) in statsMap) {
                if (currentFollowers <= 0) continue
                
                val request = RecordStatsRequest(
                    platformType = com.mtaanimation.growthos.shared.models.PlatformType.valueOf(platformStr.uppercase()),
                    currentFollowers = currentFollowers,
                    dateRecordedEpochMillis = dateEpochMillis
                )

                val result = statsRepository.recordStats(request)
                if (result.isFailure) {
                    hasError = true
                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                }
            }

            if (hasError) {
                _submitState.value = TrackingSubmitState.Error("Error recording some stats: $errorMessage")
            } else {
                _submitState.value = TrackingSubmitState.Success
                dashboardRepository.invalidateCache()
                fetchDashboard()
            }
        }
    }

    fun resetState() {
        _submitState.value = TrackingSubmitState.Idle
    }
}
