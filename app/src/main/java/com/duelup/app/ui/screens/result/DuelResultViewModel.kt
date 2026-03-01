package com.duelup.app.ui.screens.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.DuelRepository
import com.duelup.app.domain.model.Duel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuelResultUiState(
    val duelId: String = "",
    val result: String = "", // "win", "lose", "draw"
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 10,
    val avgTimeMs: Int = 0,
    val ratingChange: Int = 0,
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0,
    val quizId: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class DuelResultViewModel @Inject constructor(
    private val duelRepository: DuelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val duelId: String = savedStateHandle.get<String>("duelId") ?: ""

    private val _uiState = MutableStateFlow(DuelResultUiState(duelId = duelId))
    val uiState: StateFlow<DuelResultUiState> = _uiState.asStateFlow()

    init {
        loadDuelResult()
    }

    private fun loadDuelResult() {
        viewModelScope.launch {
            duelRepository.getDuel(duelId)
                .onSuccess { duel ->
                    _uiState.value = _uiState.value.copy(
                        playerScore = duel.player1Score,
                        opponentScore = duel.player2Score,
                        totalQuestions = duel.totalQuestions,
                        quizId = duel.quizId,
                        result = when {
                            duel.player1Score > duel.player2Score -> "win"
                            duel.player1Score < duel.player2Score -> "lose"
                            else -> "draw"
                        },
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun updateFromSocket(
        result: String,
        playerScore: Int,
        opponentScore: Int,
        correctAnswers: Int,
        avgTimeMs: Int,
        ratingChange: Int,
        xpEarned: Int,
        coinsEarned: Int
    ) {
        _uiState.value = _uiState.value.copy(
            result = result,
            playerScore = playerScore,
            opponentScore = opponentScore,
            correctAnswers = correctAnswers,
            avgTimeMs = avgTimeMs,
            ratingChange = ratingChange,
            xpEarned = xpEarned,
            coinsEarned = coinsEarned,
            isLoading = false
        )
    }
}
