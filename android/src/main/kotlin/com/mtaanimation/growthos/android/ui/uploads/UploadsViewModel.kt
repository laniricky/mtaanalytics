package com.mtaanimation.growthos.android.ui.uploads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.domain.repository.UploadsRepository
import com.mtaanimation.growthos.shared.models.uploads.RecordUploadsRequest
import com.mtaanimation.growthos.shared.models.uploads.UploadsEntryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UploadsUiState {
    data object Loading : UploadsUiState
    data class Success(val uploads: List<UploadsEntryDto>) : UploadsUiState
    data class Error(val message: String) : UploadsUiState
}

@HiltViewModel
class UploadsViewModel @Inject constructor(
    private val repository: UploadsRepository,
    settingsDataStore: com.mtaanimation.growthos.android.data.datastore.SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadsUiState>(UploadsUiState.Loading)
    
    val ytTarget = settingsDataStore.ytWeeklyTarget
    val ttTarget = settingsDataStore.ttWeeklyTarget
    val fbTarget = settingsDataStore.fbWeeklyTarget
    val igTarget = settingsDataStore.igWeeklyTarget

    val uiState: StateFlow<UploadsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UploadsUiState.Loading
            repository.getAllUploads()
                .onSuccess { uploads ->
                    _uiState.value = UploadsUiState.Success(uploads)
                }
                .onFailure { error ->
                    _uiState.value = UploadsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun recordUploads(request: RecordUploadsRequest) {
        viewModelScope.launch {
            repository.recordUploads(request)
            loadData() // refresh after successful save
        }
    }
}
