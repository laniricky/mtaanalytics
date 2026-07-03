package com.mtaanimation.growthos.android.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.RevenueRepository
import com.mtaanimation.growthos.shared.models.revenue.RevenueEntryDto
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
        val totalAllTime: Double
    ) : RevenueUiState
    data class Error(val message: String) : RevenueUiState
}

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val repository: RevenueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevenueUiState>(RevenueUiState.Loading)
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = RevenueUiState.Loading
            repository.getAllRevenue()
                .onSuccess { entries ->
                    val total = entries.sumOf { it.totalRevenue }
                    _uiState.value = RevenueUiState.Success(entries, total)
                }
                .onFailure { error ->
                    _uiState.value = RevenueUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
