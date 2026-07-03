package com.mtaanimation.growthos.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.DashboardRepository
import com.mtaanimation.growthos.shared.projection.DashboardProjection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val projection: DashboardProjection) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard(deadlineOverride: Long? = null) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            repository.getDashboard(deadlineOverride)
                .onSuccess { _uiState.value = DashboardUiState.Success(it) }
                .onFailure { _uiState.value = DashboardUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun refresh() = loadDashboard()
}
