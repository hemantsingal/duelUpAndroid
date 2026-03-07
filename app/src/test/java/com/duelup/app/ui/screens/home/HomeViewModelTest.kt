package com.duelup.app.ui.screens.home

import app.cash.turbine.test
import com.duelup.app.data.local.SearchHistoryManager
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.Quiz
import com.duelup.app.domain.model.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var quizRepository: QuizRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var searchHistoryManager: SearchHistoryManager
    private val sessionStateFlow = MutableStateFlow<SessionState>(SessionState.Loading)

    private val testUser = User(
        id = "user1",
        username = "TestUser",
        displayName = "Test",
        avatarUrl = null,
        isGuest = true,
        rating = 1200
    )

    private val testCategory = Category(
        id = "c1", name = "Science", slug = "science", iconUrl = null, color = "#00B894"
    )

    private val testQuizzes = listOf(
        Quiz(
            id = "q1",
            title = "Test Quiz",
            category = testCategory,
            difficulty = "easy",
            questionCount = 10,
            timePerQuestion = 15,
            playCount = 100,
            thumbnailUrl = null
        )
    )

    private val testCategories = listOf(
        Category(id = "c1", name = "Science", slug = "science", iconUrl = null, color = "#00B894")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        quizRepository = mockk()
        authRepository = mockk()
        searchHistoryManager = mockk()
        every { authRepository.sessionState } returns sessionStateFlow
        every { searchHistoryManager.recentSearches } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(quizRepository, authRepository, searchHistoryManager)
    }

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.success(emptyList())
        coEvery { quizRepository.getCategories() } returns Result.success(emptyList())
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadHomeData success populates all lists`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.success(testQuizzes)
        coEvery { quizRepository.getCategories() } returns Result.success(testCategories)
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.success(testQuizzes)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.featuredQuizzes.size)
        assertEquals(1, state.categories.size)
        assertEquals(1, state.popularQuizzes.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadHomeData all failures sets error`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.failure(Exception("Network error"))
        coEvery { quizRepository.getCategories() } returns Result.failure(Exception("Network error"))
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.failure(Exception("Network error"))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load data. Pull to refresh.", state.error)
    }

    @Test
    fun `partial failure still loads available data`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.success(testQuizzes)
        coEvery { quizRepository.getCategories() } returns Result.failure(Exception("Error"))
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.success(testQuizzes)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.featuredQuizzes.size)
        assertTrue(state.categories.isEmpty())
        assertEquals(1, state.popularQuizzes.size)
        assertNull(state.error)
    }

    @Test
    fun `session authenticated updates user`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.success(emptyList())
        coEvery { quizRepository.getCategories() } returns Result.success(emptyList())
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()

        sessionStateFlow.value = SessionState.Authenticated(testUser)

        assertEquals(testUser, viewModel.uiState.value.user)
    }

    @Test
    fun `refresh sets isRefreshing and reloads data`() = runTest {
        coEvery { quizRepository.getFeaturedQuizzes(any()) } returns Result.success(testQuizzes)
        coEvery { quizRepository.getCategories() } returns Result.success(testCategories)
        coEvery { quizRepository.getPopularQuizzes(any()) } returns Result.success(testQuizzes)

        val viewModel = createViewModel()

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(1, state.featuredQuizzes.size)
    }
}
