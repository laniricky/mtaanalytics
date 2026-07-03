package com.mtaanimation.growthos.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtaanimation.growthos.android.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mtaanimation.growthos.android.data.network.AuthApiService

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val ytWeeklyTarget: Int = 2,
    val ttWeeklyTarget: Int = 5,
    val fbWeeklyTarget: Int = 3,
    val igWeeklyTarget: Int = 3
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val authApiService: AuthApiService
) : ViewModel() {

    val uiState = combine(
        combine(
            settingsDataStore.isDarkMode,
            settingsDataStore.notificationsEnabled,
            settingsDataStore.ytWeeklyTarget
        ) { dark, notif, yt -> Triple(dark, notif, yt) },
        combine(
            settingsDataStore.ttWeeklyTarget,
            settingsDataStore.fbWeeklyTarget,
            settingsDataStore.igWeeklyTarget
        ) { tt, fb, ig -> Triple(tt, fb, ig) }
    ) { (dark, notif, yt), (tt, fb, ig) ->
        SettingsUiState(dark, notif, yt, tt, fb, ig)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateDarkMode(enabled: Boolean) = viewModelScope.launch {
        settingsDataStore.setDarkMode(enabled)
    }

    fun updateNotifications(enabled: Boolean) = viewModelScope.launch {
        settingsDataStore.setNotificationsEnabled(enabled)
    }

    fun updateWeeklyTargets(yt: Int, tt: Int, fb: Int, ig: Int) = viewModelScope.launch {
        settingsDataStore.setWeeklyTargets(yt, tt, fb, ig)
    }

    fun logout(onComplete: () -> Unit) = viewModelScope.launch {
        authApiService.logout()
        onComplete()
    }
}
