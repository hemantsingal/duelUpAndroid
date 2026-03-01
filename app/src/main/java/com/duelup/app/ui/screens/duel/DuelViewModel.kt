package com.duelup.app.ui.screens.duel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.remote.socket.DuelEndPayload
import com.duelup.app.data.remote.socket.QuestionPayload
import com.duelup.app.data.remote.socket.QuestionResultPayload
import com.duelup.app.data.remote.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DuelPhase {
    WAITING,       // Waiting for duel to start
    PLAYING,       // Answering a question
    REVEALING,     // Showing result of current question
    TRANSITIONING, // Between questions
    ENDED          // Duel complete
}

data class QuestionDotState(
    val index: Int,
    val playerCorrect: Boolean? = null,   // null = not answered yet
    val opponentCorrect: Boolean? = null,
    val playerTimeMs: Int = 0,
    val opponentTimeMs: Int = 0,
    val playerPoints: Int = 0,
    val opponentPoints: Int = 0
)

data class DuelUiState(
    val duelId: String = "",
    val phase: DuelPhase = DuelPhase.WAITING,
    val currentQuestion: QuestionPayload? = null,
    val totalQuestions: Int = 6,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val timerSeconds: Int = 15,
    val timerProgress: Float = 1f,
    val selectedAnswer: Int? = null,
    val isAnswerLocked: Boolean = false,
    val questionResult: QuestionResultPayload? = null,
    val correctAnswer: Int? = null,
    val pointsEarned: Int = 0,
    val streak: Int = 0,
    val opponentAnswered: Boolean = false,
    val duelEndPayload: DuelEndPayload? = null,
    val questionDots: List<QuestionDotState> = emptyList()
)

@HiltViewModel
class DuelViewModel @Inject constructor(
    private val socketManager: SocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val duelId: String = savedStateHandle.get<String>("duelId") ?: ""

    private val _uiState = MutableStateFlow(DuelUiState(duelId = duelId))
    val uiState: StateFlow<DuelUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var timeLimitSeconds = 15

    init {
        listenToEvents()
    }

    private fun initDots(totalQuestions: Int) {
        if (_uiState.value.questionDots.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                totalQuestions = totalQuestions,
                questionDots = List(totalQuestions) { QuestionDotState(index = it) }
            )
        }
    }

    private fun listenToEvents() {
        // Duel start - first question
        viewModelScope.launch {
            socketManager.onDuelStart().collect { payload ->
                timeLimitSeconds = payload.question.timeLimit
                initDots(_uiState.value.totalQuestions)
                _uiState.value = _uiState.value.copy(
                    phase = DuelPhase.PLAYING,
                    currentQuestion = payload.question,
                    selectedAnswer = null,
                    isAnswerLocked = false,
                    opponentAnswered = false,
                    questionResult = null,
                    correctAnswer = null,
                    pointsEarned = 0
                )
                startTimer(timeLimitSeconds)
            }
        }

        // Next question
        viewModelScope.launch {
            socketManager.onNextQuestion().collect { payload ->
                timeLimitSeconds = payload.question.timeLimit
                _uiState.value = _uiState.value.copy(
                    phase = DuelPhase.PLAYING,
                    currentQuestion = payload.question,
                    selectedAnswer = null,
                    isAnswerLocked = false,
                    opponentAnswered = false,
                    questionResult = null,
                    correctAnswer = null,
                    pointsEarned = 0
                )
                startTimer(timeLimitSeconds)
            }
        }

        // Opponent progress
        viewModelScope.launch {
            socketManager.onOpponentProgress().collect { payload ->
                _uiState.value = _uiState.value.copy(
                    opponentAnswered = payload.hasAnswered
                )
            }
        }

        // Question result
        viewModelScope.launch {
            socketManager.onQuestionResult().collect { result ->
                timerJob?.cancel()

                val currentStreak = if (result.player.isCorrect) {
                    _uiState.value.streak + 1
                } else 0

                // Update the dot for this question
                val updatedDots = _uiState.value.questionDots.toMutableList()
                val qIndex = result.questionIndex
                if (qIndex in updatedDots.indices) {
                    updatedDots[qIndex] = updatedDots[qIndex].copy(
                        playerCorrect = result.player.isCorrect,
                        opponentCorrect = result.opponent.isCorrect,
                        playerTimeMs = result.player.timeMs,
                        opponentTimeMs = result.opponent.timeMs,
                        playerPoints = result.player.pointsEarned,
                        opponentPoints = result.opponent.pointsEarned
                    )
                }

                _uiState.value = _uiState.value.copy(
                    phase = DuelPhase.REVEALING,
                    questionResult = result,
                    correctAnswer = result.correctAnswer,
                    pointsEarned = result.player.pointsEarned,
                    playerScore = result.player.totalScore,
                    opponentScore = result.opponent.totalScore,
                    streak = currentStreak,
                    questionDots = updatedDots
                )
            }
        }

        // Duel end
        viewModelScope.launch {
            socketManager.onDuelEnd().collect { payload ->
                timerJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    phase = DuelPhase.ENDED,
                    duelEndPayload = payload
                )
            }
        }

        // Error handling
        viewModelScope.launch {
            socketManager.onError().collect {
                // Keep playing, log error silently
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        val state = _uiState.value
        if (state.isAnswerLocked || state.phase != DuelPhase.PLAYING) return

        _uiState.value = state.copy(
            selectedAnswer = answerIndex,
            isAnswerLocked = true
        )

        socketManager.sendAnswer(
            duelId = duelId,
            questionIndex = state.currentQuestion?.index ?: 0,
            answerIndex = answerIndex
        )
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalMs = seconds * 1000L
            var remainingMs = totalMs

            while (remainingMs > 0) {
                _uiState.value = _uiState.value.copy(
                    timerSeconds = (remainingMs / 1000).toInt() + 1,
                    timerProgress = remainingMs.toFloat() / totalMs
                )
                delay(100)
                remainingMs -= 100
            }

            // Timer expired — auto-submit timeout
            _uiState.value = _uiState.value.copy(
                timerSeconds = 0,
                timerProgress = 0f,
                isAnswerLocked = true
            )

            if (_uiState.value.selectedAnswer == null) {
                socketManager.sendAnswerTimeout(
                    duelId = duelId,
                    questionIndex = _uiState.value.currentQuestion?.index ?: 0
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
