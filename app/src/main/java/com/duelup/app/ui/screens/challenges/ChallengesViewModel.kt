package com.duelup.app.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.ChallengeRepository
import com.duelup.app.domain.model.Challenge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val activeChallenges: List<Challenge>
        get() = challenges.filter { !it.isCompleted }

    val completedChallenges: List<Challenge>
        get() = challenges.filter { it.isCompleted }
}

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        loadChallenges()
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            challengeRepository.getChallenges()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        challenges = response.challenges,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load challenges"
                    )
                }
        }
    }
}
