package com.duelup.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashNavEvent {
    data object GoToHome : SplashNavEvent()
    data object GoToLogin : SplashNavEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navEvent = MutableSharedFlow<SplashNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.initialize()
            // Minimum splash display time
            delay(2000)

            when (authRepository.sessionState.value) {
                is SessionState.Authenticated -> _navEvent.emit(SplashNavEvent.GoToHome)
                else -> _navEvent.emit(SplashNavEvent.GoToLogin)
            }
        }
    }
}
