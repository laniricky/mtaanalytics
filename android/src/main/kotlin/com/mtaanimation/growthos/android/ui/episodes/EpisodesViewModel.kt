package com.mtaanimation.growthos.android.ui.episodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.EpisodeRepository
import com.mtaanimation.growthos.shared.models.episodes.CreateEpisodeRequest
import com.mtaanimation.growthos.shared.models.episodes.EpisodeDto
import com.mtaanimation.growthos.shared.models.episodes.UpsertEpisodeLinkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpisodesUiState(
    val isLoading: Boolean = true,
    val episodes: List<EpisodeDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpisodesUiState())
    val uiState: StateFlow<EpisodesUiState> = _uiState.asStateFlow()

    init {
        fetchEpisodes()
    }

    fun fetchEpisodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = episodeRepository.getEpisodes()
            result.onSuccess { episodes ->
                _uiState.update { it.copy(isLoading = false, episodes = episodes) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun createEpisode(title: String, description: String?, publishedAt: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = episodeRepository.createEpisode(
                CreateEpisodeRequest(title = title, description = description, publishedAt = publishedAt)
            )
            result.onSuccess { newEpisode ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        episodes = listOf(newEpisode) + state.episodes
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun upsertLink(episodeId: String, platform: String, url: String?, viewCount: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = episodeRepository.upsertLink(
                episodeId,
                UpsertEpisodeLinkRequest(platform = platform, url = url, viewCount = viewCount)
            )
            result.onSuccess { link ->
                _uiState.update { state ->
                    val updatedEpisodes = state.episodes.map { ep ->
                        if (ep.id == episodeId) {
                            val newLinks = ep.links.filter { it.platform != platform } + link
                            val newTotal = newLinks.sumOf { it.viewCount }
                            ep.copy(links = newLinks, totalViews = newTotal)
                        } else {
                            ep
                        }
                    }
                    state.copy(isLoading = false, episodes = updatedEpisodes)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}
