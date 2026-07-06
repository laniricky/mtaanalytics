package com.mtaanimation.growthos.android.ui.customgoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.CustomGoalsRepository
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import com.mtaanimation.growthos.shared.models.customgoals.MilestoneLiveValues
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Goal types whose currentValue is automatically computed from existing app data. */
val AUTO_TRACKED_TYPES = setOf("VIEWS", "REVENUE", "EPISODES", "FOLLOWERS")

sealed interface CustomGoalsUiState {
    data object Loading : CustomGoalsUiState
    data class Success(
        val goals: List<CustomGoalDto>,
        val liveValues: MilestoneLiveValues
    ) : CustomGoalsUiState
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

            // Fetch goals and live values in parallel
            val goalsDeferred = async { repository.getAllGoals() }
            val liveDeferred = async { repository.getLiveValues() }

            val goalsResult = goalsDeferred.await()
            val liveResult = liveDeferred.await()

            when {
                goalsResult.isFailure -> {
                    _uiState.value = CustomGoalsUiState.Error(
                        goalsResult.exceptionOrNull()?.message ?: "Failed to load goals"
                    )
                }
                liveResult.isFailure -> {
                    // Degrade gracefully — show goals with zeroed live values rather than error
                    _uiState.value = CustomGoalsUiState.Success(
                        goals = goalsResult.getOrDefault(emptyList()),
                        liveValues = MilestoneLiveValues(0L, 0L, 0.0, 0L)
                    )
                }
                else -> {
                    _uiState.value = CustomGoalsUiState.Success(
                        goals = goalsResult.getOrDefault(emptyList()),
                        liveValues = liveResult.getOrThrow()
                    )
                }
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

    /** Returns the live-computed current value for a goal based on its type. */
    fun liveCurrentValue(goal: CustomGoalDto, liveValues: MilestoneLiveValues): Double {
        return when (goal.type) {
            "VIEWS"     -> liveValues.totalViews.toDouble()
            "EPISODES"  -> liveValues.totalEpisodes.toDouble()
            "REVENUE"   -> liveValues.totalRevenue
            "FOLLOWERS" -> liveValues.totalFollowers.toDouble()
            else        -> goal.currentValue  // OTHER — use manually stored value
        }
    }
}
