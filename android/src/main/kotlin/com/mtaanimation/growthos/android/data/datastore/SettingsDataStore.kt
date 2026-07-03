package com.mtaanimation.growthos.android.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val YT_WEEKLY_TARGET = intPreferencesKey("yt_weekly_target")
        private val TT_WEEKLY_TARGET = intPreferencesKey("tt_weekly_target")
        private val FB_WEEKLY_TARGET = intPreferencesKey("fb_weekly_target")
        private val IG_WEEKLY_TARGET = intPreferencesKey("ig_weekly_target")
    }

    val isDarkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[IS_DARK_MODE] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    
    val ytWeeklyTarget: Flow<Int> = context.settingsDataStore.data.map { it[YT_WEEKLY_TARGET] ?: 2 }
    val ttWeeklyTarget: Flow<Int> = context.settingsDataStore.data.map { it[TT_WEEKLY_TARGET] ?: 5 }
    val fbWeeklyTarget: Flow<Int> = context.settingsDataStore.data.map { it[FB_WEEKLY_TARGET] ?: 3 }
    val igWeeklyTarget: Flow<Int> = context.settingsDataStore.data.map { it[IG_WEEKLY_TARGET] ?: 3 }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[IS_DARK_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setWeeklyTargets(yt: Int, tt: Int, fb: Int, ig: Int) {
        context.settingsDataStore.edit {
            it[YT_WEEKLY_TARGET] = yt
            it[TT_WEEKLY_TARGET] = tt
            it[FB_WEEKLY_TARGET] = fb
            it[IG_WEEKLY_TARGET] = ig
        }
    }
}
