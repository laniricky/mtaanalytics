package com.mtaanimation.growthos.android.ui.platforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.DashboardRepository
import com.mtaanimation.growthos.android.domain.repository.StatsRepository
import com.mtaanimation.growthos.shared.models.PlatformStatsDto
import com.mtaanimation.growthos.shared.projection.PlatformProjection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlatformDetailState(
    val projection: PlatformProjection,
    val historicalStats: List<PlatformStatsDto>
)

sealed interface PlatformsUiState {
    data object Loading : PlatformsUiState
    data class Success(val platforms: List<PlatformDetailState>) : PlatformsUiState
    data class Error(val message: String) : PlatformsUiState
}

@HiltViewModel
class PlatformsViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlatformsUiState>(PlatformsUiState.Loading)
    val uiState: StateFlow<PlatformsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PlatformsUiState.Loading
            
            // Fetch projections and stats in parallel
            val dashboardDef = async { dashboardRepository.getDashboard() }
            val statsDef = async { statsRepository.getAllStats() }
            
            val dashboardRes = dashboardDef.await()
            val statsRes = statsDef.await()
            
            if (dashboardRes.isSuccess && statsRes.isSuccess) {
                val dashboard = dashboardRes.getOrThrow()
                val allStats = statsRes.getOrThrow()
                
                val details = dashboard.platformProjections.map { proj ->
                    PlatformDetailState(
                        projection = proj,
                        historicalStats = allStats.filter { it.platformType == proj.platformType }
                            .sortedBy { it.dateRecordedEpochMillis }
                    )
                }
                
                _uiState.value = PlatformsUiState.Success(details)
            } else {
                val errorMsg = dashboardRes.exceptionOrNull()?.message 
                    ?: statsRes.exceptionOrNull()?.message 
                    ?: "Unknown error occurred"
                _uiState.value = PlatformsUiState.Error(errorMsg)
            }
        }
    }
}
