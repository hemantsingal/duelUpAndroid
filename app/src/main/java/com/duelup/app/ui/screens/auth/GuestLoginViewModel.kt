package com.duelup.app.ui.screens.auth

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuestLoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class GuestLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GuestLoginUiState())
    val uiState: StateFlow<GuestLoginUiState> = _uiState.asStateFlow()

    fun loginAsGuest() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val deviceId = Settings.Secure.getString(
                getApplication<Application>().contentResolver,
                Settings.Secure.ANDROID_ID
            )

            authRepository.guestLogin(deviceId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed. Please try again."
                    )
                }
        }
    }

    fun socialLogin(provider: String) {
        // TODO: Integrate Supabase SDK for OAuth flow
        // 1. Launch Supabase OAuth for the given provider (google/apple)
        // 2. On success, call api.linkAccount() with the Supabase token
        // 3. Store session and navigate to Home
        _uiState.value = _uiState.value.copy(
            error = "Social login coming soon! Play as guest for now."
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
