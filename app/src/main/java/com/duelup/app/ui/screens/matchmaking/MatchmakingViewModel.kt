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
import java.util.UUID
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

    companion object {
        private const val AI_MATCH_TIMEOUT_SECONDS = 3
        private val AI_BOT_NAMES = listOf(
            "QuizBot", "BrainiacAI", "TriviaBot", "SmartBot",
            "ChallengBot", "QuizMaster", "NeuroBot", "LogicBot"
        )
    }

    val currentUser: User?
        get() = (authRepository.sessionState.value as? SessionState.Authenticated)?.user

    init {
        startSearching()
    }

    private fun startSearching() {
        viewModelScope.launch {
            _state.value = MatchmakingState.Connecting

            val connected = try {
                socketManager.connect()
                delay(500)
                socketManager.isConnected()
            } catch (_: Exception) {
                false
            }

            if (connected) {
                socketManager.joinMatchmaking(quizId)
            }
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

                    // After 3 seconds with no match, auto-match with AI bot
                    if (seconds >= AI_MATCH_TIMEOUT_SECONDS && _state.value is MatchmakingState.Searching) {
                        matchWithAIBot()
                        return@launch
                    }
                }
            }

            // Listen for match found (from real opponent before timeout)
            if (connected) {
                launch {
                    socketManager.onMatchmakingFound().collect { payload ->
                        timerJob?.cancel()
                        onMatchFound(
                            duelId = payload.duelId,
                            opponent = payload.opponent,
                            quiz = payload.quiz
                        )
                        socketManager.sendReady(payload.duelId)
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
    }

    private fun matchWithAIBot() {
        timerJob?.cancel()
        val playerRating = currentUser?.rating ?: 1000
        val botRating = (playerRating - 100..playerRating + 100).random().coerceAtLeast(100)
        val botName = AI_BOT_NAMES.random()
        val aiDuelId = "ai-${UUID.randomUUID()}"

        onMatchFound(
            duelId = aiDuelId,
            opponent = MatchOpponentInfo(
                id = "ai-bot",
                username = botName,
                rating = botRating,
                isAI = true
            ),
            quiz = MatchQuizInfo(
                id = quizId,
                title = "",
                questionCount = 6,
                timePerQuestion = 15
            )
        )
    }

    private fun onMatchFound(duelId: String, opponent: MatchOpponentInfo, quiz: MatchQuizInfo) {
        _state.value = MatchmakingState.Found(
            duelId = duelId,
            opponent = opponent,
            quiz = quiz
        )

        // Countdown to duel start
        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                val current = _state.value
                if (current is MatchmakingState.Found) {
                    _state.value = current.copy(countdown = i)
                }
                delay(1000)
            }
            // Set countdown to 0 to trigger navigation
            val current = _state.value
            if (current is MatchmakingState.Found) {
                _state.value = current.copy(countdown = 0)
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
