package com.duelup.app.ui.screens.matchmaking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.remote.socket.MatchOpponentInfo
import com.duelup.app.data.remote.socket.MatchQuizInfo
import com.duelup.app.data.remote.socket.SocketManager
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MatchmakingState {
    data object Connecting : MatchmakingState()
    data class Searching(val waitTimeSeconds: Int = 0) : MatchmakingState()
    data class Found(
        val duelId: String,
        val opponent: MatchOpponentInfo,
        val quiz: MatchQuizInfo,
        val countdown: Int = 3
    ) : MatchmakingState()
    data class Error(val message: String) : MatchmakingState()
    data object Cancelled : MatchmakingState()
}

@HiltViewModel
class MatchmakingViewModel @Inject constructor(
    private val socketManager: SocketManager,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val quizId: String = savedStateHandle.get<String>("quizId") ?: ""

    private val _state = MutableStateFlow<MatchmakingState>(MatchmakingState.Connecting)
    val state: StateFlow<MatchmakingState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    val currentUser: User?
        get() = (authRepository.sessionState.value as? SessionState.Authenticated)?.user

    init {
        startSearching()
    }

    private fun startSearching() {
        viewModelScope.launch {
            _state.value = MatchmakingState.Connecting
            socketManager.connect()

            // Small delay to ensure connection
            delay(500)

            if (!socketManager.isConnected()) {
                _state.value = MatchmakingState.Error("Failed to connect to server")
                return@launch
            }

            socketManager.joinMatchmaking(quizId)
            _state.value = MatchmakingState.Searching()

            // Start wait timer
            timerJob = launch {
                var seconds = 0
                while (true) {
                    delay(1000)
                    seconds++
                    val current = _state.value
                    if (current is MatchmakingState.Searching) {
                        _state.value = current.copy(waitTimeSeconds = seconds)
                    }
                }
            }

            // Listen for match found
            launch {
                socketManager.onMatchmakingFound().collect { payload ->
                    timerJob?.cancel()
                    _state.value = MatchmakingState.Found(
                        duelId = payload.duelId,
                        opponent = payload.opponent,
                        quiz = payload.quiz
                    )

                    // Send ready signal
                    socketManager.sendReady(payload.duelId)

                    // Countdown to duel start
                    countdownJob = launch {
                        for (i in 3 downTo 1) {
                            val current = _state.value
                            if (current is MatchmakingState.Found) {
                                _state.value = current.copy(countdown = i)
                            }
                            delay(1000)
                        }
                    }
                }
            }

            // Listen for errors
            launch {
                socketManager.onError().collect { error ->
                    timerJob?.cancel()
                    _state.value = MatchmakingState.Error(error.message)
                }
            }
        }
    }

    fun cancelSearch() {
        timerJob?.cancel()
        countdownJob?.cancel()
        socketManager.leaveMatchmaking()
        socketManager.disconnect()
        _state.value = MatchmakingState.Cancelled
    }

    fun retry() {
        startSearching()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        countdownJob?.cancel()
        // Don't disconnect socket here as duel screen may need it
    }
}
