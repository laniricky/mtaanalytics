package com.mtaanimation.growthos.android.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.DashboardRepository
import com.mtaanimation.growthos.android.domain.repository.RevenueRepository
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
import com.mtaanimation.growthos.shared.projection.RevenueProjection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RevenueUiState {
    data object Loading : RevenueUiState
    data class Success(
        val history: List<RevenueEntryDto>,
        val totalAllTime: Double,
        val lastMonthRevenue: Double,
        val currentAudience: Long,
        val arpuPer1000: Double,       // Revenue per 1,000 followers
        val projected2036Monthly: Double, // ARPU * (55M / 1000)
        val projection: RevenueProjection? = null
    ) : RevenueUiState
    data class Error(val message: String) : RevenueUiState
}

sealed interface RevenueSubmitState {
    data object Idle : RevenueSubmitState
    data object Submitting : RevenueSubmitState
    data object Success : RevenueSubmitState
    data class Error(val message: String) : RevenueSubmitState
}

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val repository: RevenueRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevenueUiState>(RevenueUiState.Loading)
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    private val _submitState = MutableStateFlow<RevenueSubmitState>(RevenueSubmitState.Idle)
    val submitState: StateFlow<RevenueSubmitState> = _submitState.asStateFlow()

    companion object {
        private const val TARGET_AUDIENCE = 55_000_000L
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = RevenueUiState.Loading

            // Fetch both in parallel-ish (sequential is fine, network is fast)
            val revenueResult = repository.getAllRevenue()
            val dashboardResult = dashboardRepository.getDashboard()
            val projectionResult = repository.getRevenueProjection()

            if (revenueResult.isFailure) {
                _uiState.value = RevenueUiState.Error(revenueResult.exceptionOrNull()?.message ?: "Unknown error")
                return@launch
            }

            val entries = revenueResult.getOrNull() ?: emptyList()
            val totalAllTime = entries.sumOf { it.totalRevenue }
            val lastMonthRevenue = entries.firstOrNull()?.totalRevenue ?: 0.0

            // Current audience from dashboard
            val currentAudience = dashboardResult.getOrNull()?.combinedCurrentFollowers ?: 0L

            // ARPU: last month revenue / (audience / 1000)  → $ per 1K followers
            val arpuPer1000 = if (currentAudience > 0 && lastMonthRevenue > 0) {
                lastMonthRevenue / (currentAudience / 1000.0)
            } else 0.0

            // Projection: what we'd earn monthly at 55M with current ARPU
            val projected2036Monthly = arpuPer1000 * (TARGET_AUDIENCE / 1000.0)

            _uiState.value = RevenueUiState.Success(
                history = entries,
                totalAllTime = totalAllTime,
                lastMonthRevenue = lastMonthRevenue,
                currentAudience = currentAudience,
                arpuPer1000 = arpuPer1000,
                projected2036Monthly = projected2036Monthly,
                projection = projectionResult.getOrNull()
            )
        }
    }

    fun recordRevenue(request: RecordRevenueRequest) {
        viewModelScope.launch {
            _submitState.value = RevenueSubmitState.Submitting
            repository.recordRevenue(request)
                .onSuccess {
                    _submitState.value = RevenueSubmitState.Success
                    loadData()
                }
                .onFailure {
                    _submitState.value = RevenueSubmitState.Error(it.message ?: "Failed to save")
                }
        }
    }

    fun resetSubmitState() {
        _submitState.value = RevenueSubmitState.Idle
    }
}
