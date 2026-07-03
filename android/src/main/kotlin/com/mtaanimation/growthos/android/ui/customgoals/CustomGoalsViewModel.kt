package com.mtaanimation.growthos.android.ui.customgoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.CustomGoalsRepository
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CustomGoalsUiState {
    data object Loading : CustomGoalsUiState
    data class Success(val goals: List<CustomGoalDto>) : CustomGoalsUiState
    data class Error(val message: String) : CustomGoalsUiState
}

@HiltViewModel
class CustomGoalsViewModel @Inject constructor(
    private val repository: CustomGoalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomGoalsUiState>(CustomGoalsUiState.Loading)
    val uiState: StateFlow<CustomGoalsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CustomGoalsUiState.Loading
            repository.getAllGoals()
                .onSuccess { goals ->
                    _uiState.value = CustomGoalsUiState.Success(goals)
                }
                .onFailure { error ->
                    _uiState.value = CustomGoalsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun createGoal(request: CreateCustomGoalRequest) {
        viewModelScope.launch {
            repository.createGoal(request)
            loadData()
        }
    }

    fun updateProgress(request: UpdateCustomGoalProgressRequest) {
        viewModelScope.launch {
            repository.updateProgress(request)
            loadData()
        }
    }
}
