package com.duelup.app.ui.screens.stats

import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.UserStats
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    private val testStats = UserStats(
        totalDuels = 50,
        duelsWon = 30,
        duelsLost = 15,
        duelsDraw = 5,
        totalQuestionsAnswered = 250,
        correctAnswers = 180,
        accuracy = 0.72,
        totalXp = 5000,
        currentStreak = 3,
        bestStreak = 7,
        avgResponseTimeMs = 4500
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
    fun `init loads stats successfully`() = runTest {
        coEvery { userRepository.getStats() } returns Result.success(testStats)

        val viewModel = StatsViewModel(userRepository)

        val state = viewModel.uiState.value
        assertNotNull(state.stats)
        assertEquals(50, state.stats!!.totalDuels)
        assertEquals(30, state.stats!!.duelsWon)
        assertEquals(0.72, state.stats!!.accuracy, 0.01)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init load failure sets error`() = runTest {
        coEvery { userRepository.getStats() } returns Result.failure(Exception("Server error"))

        val viewModel = StatsViewModel(userRepository)

        val state = viewModel.uiState.value
        assertNull(state.stats)
        assertFalse(state.isLoading)
        assertEquals("Server error", state.error)
    }

    @Test
    fun `loadStats reloads data`() = runTest {
        coEvery { userRepository.getStats() } returns Result.failure(Exception("Error"))

        val viewModel = StatsViewModel(userRepository)
        assertEquals("Error", viewModel.uiState.value.error)

        coEvery { userRepository.getStats() } returns Result.success(testStats)
        viewModel.loadStats()

        val state = viewModel.uiState.value
        assertNotNull(state.stats)
        assertNull(state.error)
    }
}
