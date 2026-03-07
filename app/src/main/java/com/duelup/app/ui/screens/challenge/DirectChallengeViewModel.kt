package com.duelup.app.ui.screens.challenge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.ChallengeRepository
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.DirectChallenge
import com.duelup.app.domain.model.Quiz
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DirectChallengeState {
    data object Loading : DirectChallengeState()
    data class SelectingQuiz(val quizzes: List<Quiz>) : DirectChallengeState()
    data object Sending : DirectChallengeState()
    data class WaitingForAcceptance(val challenge: DirectChallenge) : DirectChallengeState()
    data class Error(val message: String) : DirectChallengeState()
}

@HiltViewModel
class DirectChallengeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val challengeRepository: ChallengeRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    val friendId: String = savedStateHandle["friendId"] ?: ""

    private val _state = MutableStateFlow<DirectChallengeState>(DirectChallengeState.Loading)
    val state: StateFlow<DirectChallengeState> = _state.asStateFlow()

    init {
        loadQuizzes()
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            quizRepository.getPopularQuizzes()
                .onSuccess { quizzes ->
                    _state.value = DirectChallengeState.SelectingQuiz(quizzes)
                }
                .onFailure { e ->
                    _state.value = DirectChallengeState.Error(
                        e.message ?: "Failed to load quizzes"
                    )
                }
        }
    }

    fun sendChallenge(quizId: String) {
        viewModelScope.launch {
            _state.value = DirectChallengeState.Sending
            challengeRepository.createDirectChallenge(friendId, quizId)
                .onSuccess { challenge ->
                    _state.value = DirectChallengeState.WaitingForAcceptance(challenge)
                }
                .onFailure { e ->
                    _state.value = DirectChallengeState.Error(
                        e.message ?: "Failed to send challenge"
                    )
                }
        }
    }

    fun retry() {
        loadQuizzes()
    }
}
