package com.duelup.app.ui.screens.challenges

import com.duelup.app.data.repository.ChallengeRepository
import com.duelup.app.domain.model.Challenge
import com.duelup.app.domain.model.ChallengeReward
import com.duelup.app.domain.model.ChallengesResponse
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
class ChallengesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ChallengeRepository

    private val activeChallenge = Challenge(
        id = "ch1", title = "Daily Duel", objective = "Win 3 duels today",
        currentProgress = 1, targetProgress = 3,
        reward = ChallengeReward(xp = 50),
        expiresAt = "2024-12-31T23:59:59Z", isCompleted = false
    )

    private val completedChallenge = Challenge(
        id = "ch2", title = "First Steps", objective = "Play 1 duel",
        currentProgress = 1, targetProgress = 1,
        reward = ChallengeReward(xp = 25),
        expiresAt = "2024-12-31T23:59:59Z", isCompleted = true,
        completedAt = "2024-01-01T12:00:00Z"
    )

    private val testChallenges = listOf(activeChallenge, completedChallenge)

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
    fun `init loads challenges successfully`() = runTest {
        coEvery { repository.getChallenges() } returns
                Result.success(ChallengesResponse(testChallenges))

        val viewModel = ChallengesViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(2, state.challenges.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init loads challenges failure sets error`() = runTest {
        coEvery { repository.getChallenges() } returns
                Result.failure(Exception("Server error"))

        val viewModel = ChallengesViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Server error", state.error)
    }

    @Test
    fun `activeChallenges filters correctly`() = runTest {
        coEvery { repository.getChallenges() } returns
                Result.success(ChallengesResponse(testChallenges))

        val viewModel = ChallengesViewModel(repository)

        val active = viewModel.uiState.value.activeChallenges
        assertEquals(1, active.size)
        assertEquals("Daily Duel", active.first().title)
    }

    @Test
    fun `completedChallenges filters correctly`() = runTest {
        coEvery { repository.getChallenges() } returns
                Result.success(ChallengesResponse(testChallenges))

        val viewModel = ChallengesViewModel(repository)

        val completed = viewModel.uiState.value.completedChallenges
        assertEquals(1, completed.size)
        assertEquals("First Steps", completed.first().title)
    }

    @Test
    fun `loadChallenges reloads data`() = runTest {
        coEvery { repository.getChallenges() } returns
                Result.success(ChallengesResponse(emptyList()))

        val viewModel = ChallengesViewModel(repository)
        assertEquals(0, viewModel.uiState.value.challenges.size)

        coEvery { repository.getChallenges() } returns
                Result.success(ChallengesResponse(testChallenges))

        viewModel.loadChallenges()
        assertEquals(2, viewModel.uiState.value.challenges.size)
    }
}
