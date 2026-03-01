package com.duelup.app.ui.screens.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.Quiz
import com.duelup.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizListUiState(
    val quizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val selectedDifficulty: String? = null,
    val category: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class QuizListViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizListUiState())
    val uiState: StateFlow<QuizListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        _uiState.value = _uiState.value.copy(
            category = savedStateHandle.get<String>("category")
        )
        loadQuizzes()

        viewModelScope.launch {
            _searchQuery
                .debounce(Constants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.value = _uiState.value.copy(
                        searchQuery = query,
                        currentPage = 1,
                        quizzes = emptyList(),
                        hasMore = true
                    )
                    loadQuizzes()
                }
        }
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = state.currentPage == 1, error = null)

            quizRepository.getQuizzes(
                category = state.category,
                difficulty = state.selectedDifficulty,
                search = state.searchQuery.ifBlank { null },
                page = state.currentPage,
                limit = Constants.DEFAULT_PAGE_SIZE
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    quizzes = if (state.currentPage == 1) response.quizzes
                    else _uiState.value.quizzes + response.quizzes,
                    isLoading = false,
                    isLoadingMore = false,
                    hasMore = response.quizzes.size >= Constants.DEFAULT_PAGE_SIZE
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Failed to load quizzes"
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        _uiState.value = state.copy(
            currentPage = state.currentPage + 1,
            isLoadingMore = true
        )
        loadQuizzes()
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterByDifficulty(difficulty: String?) {
        _uiState.value = _uiState.value.copy(
            selectedDifficulty = difficulty,
            currentPage = 1,
            quizzes = emptyList(),
            hasMore = true
        )
        loadQuizzes()
    }
}
