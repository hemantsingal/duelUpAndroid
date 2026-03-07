package com.duelup.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val soundEnabledKey = booleanPreferencesKey("sound_enabled")
    private val hapticEnabledKey = booleanPreferencesKey("haptic_enabled")

    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[soundEnabledKey] ?: true }
    val hapticEnabled: Flow<Boolean> = dataStore.data.map { it[hapticEnabledKey] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[soundEnabledKey] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[hapticEnabledKey] = enabled }
    }
}
