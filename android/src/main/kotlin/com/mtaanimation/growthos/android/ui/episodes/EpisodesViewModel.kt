package com.mtaanimation.growthos.android.ui.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.EpisodesRepository
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EpisodesUiState {
    data object Loading : EpisodesUiState
    data class Success(val episodes: List<EpisodeDto>) : EpisodesUiState
    data class Error(val message: String) : EpisodesUiState
}

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val repository: EpisodesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EpisodesUiState>(EpisodesUiState.Loading)
    val uiState: StateFlow<EpisodesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = EpisodesUiState.Loading
            repository.getAllEpisodes()
                .onSuccess { episodes ->
                    _uiState.value = EpisodesUiState.Success(episodes)
                }
                .onFailure { error ->
                    _uiState.value = EpisodesUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
