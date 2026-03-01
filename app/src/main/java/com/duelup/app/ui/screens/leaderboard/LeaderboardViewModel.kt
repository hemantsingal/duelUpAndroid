package com.duelup.app.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.LeaderboardEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeaderboardTab { GLOBAL, WEEKLY }

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUser: LeaderboardEntry? = null,
    val selectedTab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun switchTab(tab: LeaderboardTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = when (_uiState.value.selectedTab) {
                LeaderboardTab.GLOBAL -> userRepository.getGlobalLeaderboard()
                LeaderboardTab.WEEKLY -> userRepository.getWeeklyLeaderboard()
            }

            result
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        entries = response.entries,
                        currentUser = response.currentUser,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load leaderboard"
                    )
                }
        }
    }
}
