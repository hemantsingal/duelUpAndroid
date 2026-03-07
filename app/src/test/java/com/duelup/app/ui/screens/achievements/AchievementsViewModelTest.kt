package com.duelup.app.ui.screens.achievements

import com.duelup.app.data.repository.AchievementRepository
import com.duelup.app.domain.model.Achievement
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AchievementRepository

    private val testAchievements = listOf(
        Achievement(
            key = "first_win", name = "First Win", description = "Win your first duel",
            category = "milestones", unlockedAt = "2024-01-01T00:00:00Z"
        ),
        Achievement(
            key = "sharp_shooter", name = "Sharp Shooter", description = "Get 10 perfect scores",
            category = "accuracy", unlockedAt = "2024-02-01T00:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads achievements successfully`() = runTest {
        coEvery { repository.getAchievements() } returns Result.success(testAchievements)

        val viewModel = AchievementsViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(2, state.achievements.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init loads achievements failure sets error`() = runTest {
        coEvery { repository.getAchievements() } returns
                Result.failure(Exception("Network error"))

        val viewModel = AchievementsViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `loadAchievements reloads data`() = runTest {
        coEvery { repository.getAchievements() } returns Result.success(emptyList())

        val viewModel = AchievementsViewModel(repository)
        assertEquals(0, viewModel.uiState.value.achievements.size)

        coEvery { repository.getAchievements() } returns Result.success(testAchievements)

        viewModel.loadAchievements()
        assertEquals(2, viewModel.uiState.value.achievements.size)
    }
}
