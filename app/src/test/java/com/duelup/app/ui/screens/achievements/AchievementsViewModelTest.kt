package com.duelup.app.ui.screens.achievements

import com.duelup.app.data.repository.AchievementRepository
import com.duelup.app.domain.model.Achievement
import com.duelup.app.domain.model.AchievementsResponse
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

    private val unlockedAchievement = Achievement(
        id = "a1", name = "First Win", description = "Win your first duel",
        category = "milestones", pointsAwarded = 50, isUnlocked = true
    )

    private val lockedAchievement = Achievement(
        id = "a2", name = "Sharp Shooter", description = "Get 10 perfect scores",
        category = "accuracy", pointsAwarded = 100, isUnlocked = false
    )

    private val testAchievements = listOf(unlockedAchievement, lockedAchievement)

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
        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(testAchievements))

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
    fun `selectTab ALL shows all achievements`() = runTest {
        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(testAchievements))

        val viewModel = AchievementsViewModel(repository)
        viewModel.selectTab(AchievementTab.ALL)

        assertEquals(2, viewModel.uiState.value.filteredAchievements.size)
    }

    @Test
    fun `selectTab UNLOCKED filters to unlocked only`() = runTest {
        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(testAchievements))

        val viewModel = AchievementsViewModel(repository)
        viewModel.selectTab(AchievementTab.UNLOCKED)

        val filtered = viewModel.uiState.value.filteredAchievements
        assertEquals(1, filtered.size)
        assertEquals("First Win", filtered.first().name)
    }

    @Test
    fun `selectTab LOCKED filters to locked only`() = runTest {
        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(testAchievements))

        val viewModel = AchievementsViewModel(repository)
        viewModel.selectTab(AchievementTab.LOCKED)

        val filtered = viewModel.uiState.value.filteredAchievements
        assertEquals(1, filtered.size)
        assertEquals("Sharp Shooter", filtered.first().name)
    }

    @Test
    fun `loadAchievements reloads data`() = runTest {
        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(emptyList()))

        val viewModel = AchievementsViewModel(repository)
        assertEquals(0, viewModel.uiState.value.achievements.size)

        coEvery { repository.getAchievements() } returns
                Result.success(AchievementsResponse(testAchievements))

        viewModel.loadAchievements()
        assertEquals(2, viewModel.uiState.value.achievements.size)
    }
}
