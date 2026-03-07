package com.duelup.app.ui.screens.home

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
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
    val categories: List<Category> = emptyList(),
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
    private val searchHistoryManager: SearchHistoryManager,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        initializeAndLoad()
        observeSession()
        observeSearch()
        observeRecentSearches()
    }

    private fun initializeAndLoad() {
        viewModelScope.launch {
            // 1. Try restoring existing session
            try {
                authRepository.initialize()
            } catch (_: Exception) { }

            // 2. If not authenticated, auto guest login
            if (authRepository.sessionState.value !is SessionState.Authenticated) {
                val deviceId = Settings.Secure.getString(
                    getApplication<Application>().contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                authRepository.guestLogin(deviceId)
            }

            // 3. Now load data
            loadHomeData()
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

            val randomDeferred = async { quizRepository.getRandomQuizzes(6) }
            val categoriesDeferred = async { quizRepository.getCategories() }

            val random = randomDeferred.await()
            val categories = categoriesDeferred.await()

            _uiState.value = _uiState.value.copy(
                randomQuizzes = random.getOrDefault(emptyList()),
                categories = categories.getOrDefault(emptyList()),
                isLoading = false,
                isRefreshing = false,
                error = if (random.isFailure && categories.isFailure) {
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
        return _uiState.value.randomQuizzes.randomOrNull()?.id
    }
}
