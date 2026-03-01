package com.duelup.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val stats: UserStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState(isLoading = true)
            userRepository.getStats()
                .onSuccess { stats ->
                    _uiState.value = StatsUiState(stats = stats, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = StatsUiState(
                        isLoading = false,
                        error = e.message ?: "Failed to load stats"
                    )
                }
        }
    }
}
