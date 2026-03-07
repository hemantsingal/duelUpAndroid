package com.duelup.app.ui.screens.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.SessionManager
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.repository.DuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionBreakdown(
    val index: Int,
    val playerCorrect: Boolean,
    val opponentCorrect: Boolean?, // null = no data (e.g. AI opponent)
    val playerTimeMs: Int,
    val opponentTimeMs: Int,
    val playerPoints: Int,
    val opponentPoints: Int
)

data class DuelResultUiState(
    val duelId: String = "",
    val result: String = "", // "win", "lose", "draw"
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val playerName: String = "You",
    val opponentName: String = "Opponent",
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 6,
    val avgTimeMs: Int = 0,
    val ratingChange: Int = 0,
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0,
    val quizId: String = "",
    val isLoading: Boolean = true,
    val questionBreakdowns: List<QuestionBreakdown> = emptyList()
)

@HiltViewModel
class DuelResultViewModel @Inject constructor(
    private val duelRepository: DuelRepository,
    private val sessionManager: SessionManager,
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
            val currentUserId = (sessionManager.sessionState.value as? SessionState.Authenticated)?.user?.id

            duelRepository.getDuel(duelId)
                .onSuccess { duel ->
                    val isPlayer1 = currentUserId != null && currentUserId == duel.player1.id
                    val pScore = if (isPlayer1) duel.player1Score else duel.player2Score
                    val oScore = if (isPlayer1) duel.player2Score else duel.player1Score
                    val pName = if (isPlayer1) duel.player1.username else (duel.player2?.username ?: "Opponent")
                    val oName = if (isPlayer1) (duel.player2?.username ?: "Opponent") else duel.player1.username

                    _uiState.value = _uiState.value.copy(
                        playerScore = pScore,
                        opponentScore = oScore,
                        playerName = pName,
                        opponentName = oName,
                        totalQuestions = duel.totalQuestions,
                        quizId = duel.quizId,
                        result = when {
                            pScore > oScore -> "win"
                            pScore < oScore -> "lose"
                            else -> "draw"
                        },
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }

            // Load replay data for per-question breakdown
            duelRepository.getDuelReplay(duelId)
                .onSuccess { replay ->
                    val isPlayer1 = currentUserId != null && currentUserId == replay.duel.player1.id
                    val myAnswers = if (isPlayer1) replay.player1Answers else replay.player2Answers
                    val theirAnswers = if (isPlayer1) replay.player2Answers else replay.player1Answers
                    val totalQ = maxOf(myAnswers.size, theirAnswers.size)

                    // Index answers by questionIndex for correct matching
                    val myByIndex = myAnswers.associateBy { it.questionIndex }
                    val theirByIndex = theirAnswers.associateBy { it.questionIndex }

                    val breakdowns = (0 until totalQ).map { i ->
                        val playerAnswer = myByIndex[i]
                        val opponentAnswer = theirByIndex[i]
                        QuestionBreakdown(
                            index = i,
                            playerCorrect = playerAnswer?.isCorrect ?: false,
                            opponentCorrect = opponentAnswer?.isCorrect,
                            playerTimeMs = playerAnswer?.responseTimeMs ?: 0,
                            opponentTimeMs = opponentAnswer?.responseTimeMs ?: 0,
                            playerPoints = playerAnswer?.pointsEarned ?: 0,
                            opponentPoints = opponentAnswer?.pointsEarned ?: 0
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        questionBreakdowns = breakdowns,
                        correctAnswers = breakdowns.count { it.playerCorrect },
                        totalQuestions = totalQ
                    )
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
