package com.duelup.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.DuelHistoryItem
import com.duelup.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuelHistoryUiState(
    val duels: List<DuelHistoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DuelHistoryViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuelHistoryUiState())
    val uiState: StateFlow<DuelHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = state.currentPage == 1, error = null)

            userRepository.getDuelHistory(state.currentPage, Constants.DEFAULT_PAGE_SIZE)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        duels = if (state.currentPage == 1) response.duels
                        else _uiState.value.duels + response.duels,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = response.duels.size >= Constants.DEFAULT_PAGE_SIZE
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message
                    )
                }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        _uiState.value = _uiState.value.copy(
            currentPage = _uiState.value.currentPage + 1,
            isLoadingMore = true
        )
        loadHistory()
    }
}
