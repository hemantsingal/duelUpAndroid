package com.duelup.app.ui.screens.history

import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.DuelHistoryItem
import com.duelup.app.domain.model.DuelHistoryResponse
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DuelHistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    private fun createDuel(id: String, result: String) = DuelHistoryItem(
        id = id,
        quizTitle = "Quiz $id",
        opponentUsername = "Opponent",
        opponentAvatarUrl = null,
        playerScore = 500,
        opponentScore = 400,
        result = result,
        completedAt = "2024-01-01T12:00:00"
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
    fun `init loads first page of history`() = runTest {
        val duels = (1..5).map { createDuel("d$it", "win") }
        coEvery { userRepository.getDuelHistory(1, any()) } returns Result.success(
            DuelHistoryResponse(duels = duels, total = 20, page = 1, limit = 20)
        )

        val viewModel = DuelHistoryViewModel(userRepository)

        val state = viewModel.uiState.value
        assertEquals(5, state.duels.size)
        assertFalse(state.isLoading)
        assertFalse(state.hasMore) // 5 < DEFAULT_PAGE_SIZE
    }

    @Test
    fun `loadMore appends next page`() = runTest {
        val page1 = (1..20).map { createDuel("d$it", "win") }
        val page2 = (21..25).map { createDuel("d$it", "loss") }

        coEvery { userRepository.getDuelHistory(1, any()) } returns Result.success(
            DuelHistoryResponse(duels = page1, total = 25, page = 1, limit = 20)
        )
        coEvery { userRepository.getDuelHistory(2, any()) } returns Result.success(
            DuelHistoryResponse(duels = page2, total = 25, page = 2, limit = 20)
        )

        val viewModel = DuelHistoryViewModel(userRepository)
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(25, state.duels.size)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `loadMore does not trigger when hasMore is false`() = runTest {
        val duels = (1..5).map { createDuel("d$it", "win") }
        coEvery { userRepository.getDuelHistory(1, any()) } returns Result.success(
            DuelHistoryResponse(duels = duels, total = 5, page = 1, limit = 20)
        )

        val viewModel = DuelHistoryViewModel(userRepository)
        // hasMore should be false since 5 < DEFAULT_PAGE_SIZE
        assertFalse(viewModel.uiState.value.hasMore)

        viewModel.loadMore()

        // Page should not have incremented
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `load failure sets error`() = runTest {
        coEvery { userRepository.getDuelHistory(any(), any()) } returns
                Result.failure(Exception("Connection error"))

        val viewModel = DuelHistoryViewModel(userRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Connection error", state.error)
    }
}
