package com.duelup.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.UserPreferences
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.soundEnabled,
                userPreferences.hapticEnabled
            ) { sound, haptic ->
                SettingsUiState(soundEnabled = sound, hapticEnabled = haptic)
            }.collect { _uiState.value = it }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSoundEnabled(enabled)
            soundManager.setEnabled(enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setHapticEnabled(enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutEvent.emit(Unit)
        }
    }
}
