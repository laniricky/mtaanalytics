package com.mtaanimation.growthos.android.ui.customgoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.CustomGoalsRepository
import com.mtaanimation.growthos.shared.models.customgoals.MilestoneLiveValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MilestonesUiState {
    data object Loading : MilestonesUiState
    data class Success(
        val categories: List<CategoryProgress>,
        val liveValues: MilestoneLiveValues
    ) : MilestonesUiState
    data class Error(val message: String) : MilestonesUiState
}

@HiltViewModel
class CustomGoalsViewModel @Inject constructor(
    private val repository: CustomGoalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MilestonesUiState>(MilestonesUiState.Loading)
    val uiState: StateFlow<MilestonesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = MilestonesUiState.Loading
            repository.getLiveValues()
                .onSuccess { liveValues ->
                    val categories = listOf(
                        MilestoneLadder.computeProgress(
                            MilestoneLadder.FOLLOWERS,
                            liveValues.totalFollowers.toDouble()
                        ),
                        MilestoneLadder.computeProgress(
                            MilestoneLadder.VIEWS,
                            liveValues.totalViews.toDouble()
                        ),
                        MilestoneLadder.computeProgress(
                            MilestoneLadder.REVENUE,
                            liveValues.totalRevenue
                        ),
                        MilestoneLadder.computeProgress(
                            MilestoneLadder.EPISODES,
                            liveValues.totalEpisodes.toDouble()
                        )
                    )
                    _uiState.value = MilestonesUiState.Success(
                        categories = categories,
                        liveValues = liveValues
                    )
                }
                .onFailure { error ->
                    _uiState.value = MilestonesUiState.Error(
                        error.message ?: "Failed to load milestones"
                    )
                }
        }
    }
}

// Keep old sealed interface alias so BottomNavBar references don't break
typealias CustomGoalsUiState = MilestonesUiState
