package com.duelup.app.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duelup.app.data.repository.AchievementRepository
import com.duelup.app.domain.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AchievementTab { ALL, UNLOCKED, LOCKED }

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val selectedTab: AchievementTab = AchievementTab.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredAchievements: List<Achievement>
        get() = when (selectedTab) {
            AchievementTab.ALL -> achievements
            AchievementTab.UNLOCKED -> achievements.filter { it.isUnlocked }
            AchievementTab.LOCKED -> achievements.filter { !it.isUnlocked }
        }
}

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            achievementRepository.getAchievements()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        achievements = response.achievements,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load achievements"
                    )
                }
        }
    }

    fun selectTab(tab: AchievementTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}
