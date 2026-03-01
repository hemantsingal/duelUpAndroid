package com.duelup.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.remote.dto.UpdateProfileRequest
import com.duelup.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val usernameError: String? = null,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        username = profile.username,
                        displayName = profile.displayName ?: "",
                        avatarUrl = profile.avatarUrl,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun updateUsername(value: String) {
        val error = when {
            value.length < 3 -> "Username must be at least 3 characters"
            value.length > 30 -> "Username must be at most 30 characters"
            else -> null
        }
        _uiState.value = _uiState.value.copy(username = value, usernameError = error)
    }

    fun updateDisplayName(value: String) {
        _uiState.value = _uiState.value.copy(
            displayName = value.take(50)
        )
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.usernameError != null || state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            userRepository.updateProfile(
                UpdateProfileRequest(
                    username = state.username,
                    displayName = state.displayName.ifBlank { null },
                    avatarUrl = state.avatarUrl
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save profile"
                )
            }
        }
    }
}
