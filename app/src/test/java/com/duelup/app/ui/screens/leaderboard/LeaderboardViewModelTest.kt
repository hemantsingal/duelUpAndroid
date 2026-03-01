package com.duelup.app.ui.screens.leaderboard

import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.LeaderboardEntry
import com.duelup.app.domain.model.LeaderboardResponse
import io.mockk.coEvery
import io.mockk.coVerify
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
class LeaderboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    private val testEntries = listOf(
        LeaderboardEntry(
            rank = 1, userId = "u1", username = "Player1",
            rating = 1500, wins = 50, totalGames = 80, winRate = 0.625
        ),
        LeaderboardEntry(
            rank = 2, userId = "u2", username = "Player2",
            rating = 1400, wins = 40, totalGames = 70, winRate = 0.571
        )
    )

    private val testCurrentUser = LeaderboardEntry(
        rank = 42, userId = "me", username = "Me",
        rating = 1200, wins = 10, totalGames = 20, winRate = 0.5
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads global leaderboard`() = runTest {
        coEvery { userRepository.getGlobalLeaderboard(any()) } returns Result.success(
            LeaderboardResponse(entries = testEntries, currentUser = testCurrentUser)
        )

        val viewModel = LeaderboardViewModel(userRepository)

        val state = viewModel.uiState.value
        assertEquals(2, state.entries.size)
        assertEquals(testCurrentUser, state.currentUser)
        assertFalse(state.isLoading)
        assertEquals(LeaderboardTab.GLOBAL, state.selectedTab)
    }

    @Test
    fun `switchTab to weekly loads weekly leaderboard`() = runTest {
        coEvery { userRepository.getGlobalLeaderboard(any()) } returns Result.success(
            LeaderboardResponse(entries = testEntries, currentUser = testCurrentUser)
        )
        coEvery { userRepository.getWeeklyLeaderboard(any()) } returns Result.success(
            LeaderboardResponse(entries = listOf(testEntries[0]), currentUser = null)
        )

        val viewModel = LeaderboardViewModel(userRepository)
        viewModel.switchTab(LeaderboardTab.WEEKLY)

        val state = viewModel.uiState.value
        assertEquals(LeaderboardTab.WEEKLY, state.selectedTab)
        assertEquals(1, state.entries.size)
        assertNull(state.currentUser)
        coVerify { userRepository.getWeeklyLeaderboard(any()) }
    }

    @Test
    fun `load failure sets error`() = runTest {
        coEvery { userRepository.getGlobalLeaderboard(any()) } returns
                Result.failure(Exception("Network error"))

        val viewModel = LeaderboardViewModel(userRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `switchTab back to global reloads global data`() = runTest {
        coEvery { userRepository.getGlobalLeaderboard(any()) } returns Result.success(
            LeaderboardResponse(entries = testEntries, currentUser = testCurrentUser)
        )
        coEvery { userRepository.getWeeklyLeaderboard(any()) } returns Result.success(
            LeaderboardResponse(entries = emptyList(), currentUser = null)
        )

        val viewModel = LeaderboardViewModel(userRepository)
        viewModel.switchTab(LeaderboardTab.WEEKLY)
        viewModel.switchTab(LeaderboardTab.GLOBAL)

        val state = viewModel.uiState.value
        assertEquals(LeaderboardTab.GLOBAL, state.selectedTab)
        assertEquals(2, state.entries.size)
    }
}
