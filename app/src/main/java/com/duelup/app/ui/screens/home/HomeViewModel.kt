package com.duelup.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.local.SearchHistoryManager
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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val featuredQuizzes: List<Quiz> = emptyList(),
    val categories: List<Category> = emptyList(),
    val popularQuizzes: List<Quiz> = emptyList(),
    val randomQuizzes: List<Quiz> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Quiz> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val recentSearches: List<String> = emptyList()
) {
    val isSearchActive: Boolean get() = searchQuery.isNotBlank()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authRepository: AuthRepository,
    private val searchHistoryManager: SearchHistoryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        initializeAuth()
        observeSession()
        loadHomeData()
        observeSearch()
        observeRecentSearches()
    }

    private fun initializeAuth() {
        viewModelScope.launch {
            try {
                authRepository.initialize()
            } catch (_: Exception) {
                // Auth init failed silently - user can log in from drawer
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    user = if (state is SessionState.Authenticated) state.user else null,
                    isLoggedIn = state is SessionState.Authenticated
                )
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

            // Pick 5 random quizzes from popular + featured combined
            val allQuizzes = (popular.getOrDefault(emptyList()) + featured.getOrDefault(emptyList()))
                .distinctBy { it.id }
                .shuffled()
                .take(5)

            _uiState.value = _uiState.value.copy(
                featuredQuizzes = featured.getOrDefault(emptyList()),
                categories = categories.getOrDefault(emptyList()),
                popularQuizzes = popular.getOrDefault(emptyList()),
                randomQuizzes = allQuizzes,
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
                .debounce { query ->
                    if (query.length <= 2) Constants.SEARCH_DEBOUNCE_SHORT_MS
                    else Constants.SEARCH_DEBOUNCE_LONG_MS
                }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isSearching = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isSearching = true)
                        quizRepository.searchQuizzes(query = query)
                            .onSuccess { quizzes ->
                                _uiState.value = _uiState.value.copy(
                                    searchResults = quizzes,
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

    private fun observeRecentSearches() {
        viewModelScope.launch {
            searchHistoryManager.recentSearches.collect { searches ->
                _uiState.value = _uiState.value.copy(recentSearches = searches)
            }
        }
    }

    fun onSearchSubmitted(query: String) {
        viewModelScope.launch {
            searchHistoryManager.addSearch(query)
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            searchHistoryManager.removeSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            searchHistoryManager.clearAll()
        }
    }

    fun getRandomQuizId(): String? {
        val quizzes = _uiState.value.popularQuizzes.ifEmpty {
            _uiState.value.featuredQuizzes
        }
        return quizzes.randomOrNull()?.id
    }
}
