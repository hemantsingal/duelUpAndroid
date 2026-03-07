package com.duelup.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.Quiz
import com.duelup.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import com.duelup.app.util.Constants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val featuredQuizzes: List<Quiz> = emptyList(),
    val categories: List<Category> = emptyList(),
    val popularQuizzes: List<Quiz> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Quiz> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isSearchActive: Boolean get() = searchQuery.isNotBlank()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeSession()
        loadHomeData()
        observeSearch()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionState.collect { state ->
                if (state is SessionState.Authenticated) {
                    _uiState.value = _uiState.value.copy(user = state.user)
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val featuredDeferred = async { quizRepository.getFeaturedQuizzes() }
            val categoriesDeferred = async { quizRepository.getCategories() }
            val popularDeferred = async { quizRepository.getPopularQuizzes() }

            val featured = featuredDeferred.await()
            val categories = categoriesDeferred.await()
            val popular = popularDeferred.await()

            _uiState.value = _uiState.value.copy(
                featuredQuizzes = featured.getOrDefault(emptyList()),
                categories = categories.getOrDefault(emptyList()),
                popularQuizzes = popular.getOrDefault(emptyList()),
                isLoading = false,
                isRefreshing = false,
                error = if (featured.isFailure && categories.isFailure && popular.isFailure) {
                    "Failed to load data. Pull to refresh."
                } else null
            )
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadHomeData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(Constants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isSearching = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isSearching = true)
                        quizRepository.getQuizzes(search = query)
                            .onSuccess { response ->
                                _uiState.value = _uiState.value.copy(
                                    searchResults = response.quizzes,
                                    isSearching = false
                                )
                            }
                            .onFailure {
                                _uiState.value = _uiState.value.copy(isSearching = false)
                            }
                    }
                }
        }
    }

    fun getRandomQuizId(): String? {
        val quizzes = _uiState.value.popularQuizzes.ifEmpty {
            _uiState.value.featuredQuizzes
        }
        return quizzes.randomOrNull()?.id
    }
}
