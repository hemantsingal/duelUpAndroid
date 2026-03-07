package com.duelup.app.ui.screens.challenge

import androidx.lifecycle.SavedStateHandle
import com.duelup.app.data.repository.ChallengeRepository
import com.duelup.app.data.repository.QuizRepository
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.DirectChallenge
import com.duelup.app.domain.model.Quiz
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DirectChallengeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var quizRepository: QuizRepository

    private val testCategory = Category(
        id = "c1", name = "Science", slug = "science", iconUrl = null, color = "#00B894"
    )

    private val testQuizzes = listOf(
        Quiz(
            id = "q1", title = "Science Quiz", category = testCategory,
            difficulty = "easy", questionCount = 10, timePerQuestion = 15,
            playCount = 100, thumbnailUrl = null
        ),
        Quiz(
            id = "q2", title = "Math Quiz", category = testCategory,
            difficulty = "medium", questionCount = 15, timePerQuestion = 20,
            playCount = 50, thumbnailUrl = null
        )
    )

    private val testDirectChallenge = DirectChallenge(
        id = "dc1", challengerId = "me", challengerUsername = "Me",
        challengeeId = "friend1", challengeeUsername = "Friend",
        quizId = "q1", quizTitle = "Science Quiz",
        status = "pending", createdAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        challengeRepository = mockk()
        quizRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(friendId: String = "friend1"): DirectChallengeViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("friendId" to friendId))
        return DirectChallengeViewModel(savedStateHandle, challengeRepository, quizRepository)
    }

    @Test
    fun `init loads quizzes and transitions to SelectingQuiz`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns Result.success(testQuizzes)

        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertTrue(state is DirectChallengeState.SelectingQuiz)
        assertEquals(2, (state as DirectChallengeState.SelectingQuiz).quizzes.size)
    }

    @Test
    fun `init loads quizzes failure transitions to Error`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns
                Result.failure(Exception("Failed to load"))

        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertTrue(state is DirectChallengeState.Error)
        assertEquals("Failed to load", (state as DirectChallengeState.Error).message)
    }

    @Test
    fun `friendId is extracted from SavedStateHandle`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns Result.success(emptyList())

        val viewModel = createViewModel("user123")

        assertEquals("user123", viewModel.friendId)
    }

    @Test
    fun `sendChallenge success transitions to WaitingForAcceptance`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns Result.success(testQuizzes)
        coEvery { challengeRepository.createDirectChallenge("friend1", "q1") } returns
                Result.success(testDirectChallenge)

        val viewModel = createViewModel()
        viewModel.sendChallenge("q1")

        val state = viewModel.state.value
        assertTrue(state is DirectChallengeState.WaitingForAcceptance)
        assertEquals("dc1", (state as DirectChallengeState.WaitingForAcceptance).challenge.id)
    }

    @Test
    fun `sendChallenge failure transitions to Error`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns Result.success(testQuizzes)
        coEvery { challengeRepository.createDirectChallenge("friend1", "q1") } returns
                Result.failure(Exception("Challenge failed"))

        val viewModel = createViewModel()
        viewModel.sendChallenge("q1")

        val state = viewModel.state.value
        assertTrue(state is DirectChallengeState.Error)
        assertEquals("Challenge failed", (state as DirectChallengeState.Error).message)
    }

    @Test
    fun `retry reloads quizzes`() = runTest {
        coEvery { quizRepository.getPopularQuizzes() } returns
                Result.failure(Exception("Error"))

        val viewModel = createViewModel()
        assertTrue(viewModel.state.value is DirectChallengeState.Error)

        coEvery { quizRepository.getPopularQuizzes() } returns Result.success(testQuizzes)
        viewModel.retry()

        assertTrue(viewModel.state.value is DirectChallengeState.SelectingQuiz)
    }
}
