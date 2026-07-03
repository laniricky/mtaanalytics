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

    fun recordMonthlyStats(
        platformType: String,
        currentFollowers: Long,
        target2036: Long,
        dateEpochMillis: Long
    ) {
        viewModelScope.launch {
            _submitState.value = TrackingSubmitState.Submitting
            
            val request = RecordStatsRequest(
                platformType = com.mtaanimation.growthos.shared.models.PlatformType.valueOf(platformType.uppercase()),
                currentFollowers = currentFollowers,
                target2036 = target2036,
                dateRecordedEpochMillis = dateEpochMillis
            )
            
            val result = statsRepository.recordStats(request)
            
            if (result.isSuccess) {
                _submitState.value = TrackingSubmitState.Success
                // Optionally force dashboard refresh if it was cached
            } else {
                _submitState.value = TrackingSubmitState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error recording stats"
                )
            }
        }
    }

    fun resetState() {
        _submitState.value = TrackingSubmitState.Idle
    }
}
