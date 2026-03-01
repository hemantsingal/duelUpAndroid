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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val featuredQuizzes: List<Quiz> = emptyList(),
    val categories: List<Category> = emptyList(),
    val popularQuizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        loadHomeData()
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
}
