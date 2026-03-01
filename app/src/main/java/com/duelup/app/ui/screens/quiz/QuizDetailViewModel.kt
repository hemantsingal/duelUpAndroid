package com.duelup.app.ui.screens.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.QuizDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizDetailUiState(
    val quiz: QuizDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quizId: String = savedStateHandle.get<String>("quizId") ?: ""

    private val _uiState = MutableStateFlow(QuizDetailUiState())
    val uiState: StateFlow<QuizDetailUiState> = _uiState.asStateFlow()

    init {
        loadQuizDetail()
    }

    fun loadQuizDetail() {
        viewModelScope.launch {
            _uiState.value = QuizDetailUiState(isLoading = true)

            quizRepository.getQuizDetail(quizId)
                .onSuccess { quiz ->
                    _uiState.value = QuizDetailUiState(quiz = quiz, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = QuizDetailUiState(
                        isLoading = false,
                        error = e.message ?: "Failed to load quiz"
                    )
                }
        }
    }
}
