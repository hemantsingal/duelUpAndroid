package com.duelup.app.ui.screens.replay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.DuelRepository
import com.duelup.app.domain.model.DuelAnswer
import com.duelup.app.domain.model.DuelReplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuelReplayUiState(
    val replay: DuelReplay? = null,
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentPlayerAnswer: DuelAnswer?
        get() = replay?.player1Answers?.getOrNull(currentIndex)

    val currentOpponentAnswer: DuelAnswer?
        get() = replay?.player2Answers?.getOrNull(currentIndex)

    val totalQuestions: Int
        get() = replay?.duel?.totalQuestions ?: 0

    val canGoNext: Boolean
        get() = currentIndex < totalQuestions - 1

    val canGoPrevious: Boolean
        get() = currentIndex > 0
}

@HiltViewModel
class DuelReplayViewModel @Inject constructor(
    private val duelRepository: DuelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val duelId: String = savedStateHandle.get<String>("duelId") ?: ""

    private val _uiState = MutableStateFlow(DuelReplayUiState())
    val uiState: StateFlow<DuelReplayUiState> = _uiState.asStateFlow()

    init {
        loadReplay()
    }

    fun loadReplay() {
        viewModelScope.launch {
            _uiState.value = DuelReplayUiState(isLoading = true)
            duelRepository.getDuelReplay(duelId)
                .onSuccess { replay ->
                    _uiState.value = DuelReplayUiState(replay = replay, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = DuelReplayUiState(
                        isLoading = false,
                        error = e.message ?: "Failed to load replay"
                    )
                }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.canGoNext) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        if (state.canGoPrevious) {
            _uiState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }
}
