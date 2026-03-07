package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.Quiz
import com.duelup.app.domain.model.QuizDetail
import com.duelup.app.domain.model.QuizListResponse
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
    private lateinit var quizRepository: QuizRepository

    private val testCategory = Category(
        id = "c1", name = "Science", slug = "science", iconUrl = null, color = "#00B894"
    )

    private val testQuiz = Quiz(
        id = "q1",
        title = "Science Quiz",
        category = testCategory,
        difficulty = "easy",
        questionCount = 10,
        timePerQuestion = 15,
        playCount = 500,
        thumbnailUrl = null
    )

    private val testCategories = listOf(
        Category(id = "c1", name = "Science", slug = "science", iconUrl = null, color = "#00B894"),
        Category(id = "c2", name = "History", slug = "history", iconUrl = null, color = "#6C5CE7")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        quizRepository = QuizRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFeaturedQuizzes success returns quiz list`() = runTest {
        coEvery { api.getQuizzes(featured = true, limit = 5) } returns
                QuizListResponse(quizzes = listOf(testQuiz), total = 1, page = 1, limit = 5)

        val result = quizRepository.getFeaturedQuizzes()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Science Quiz", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `getFeaturedQuizzes failure returns error`() = runTest {
        coEvery { api.getQuizzes(featured = true, limit = any()) } throws Exception("Timeout")

        val result = quizRepository.getFeaturedQuizzes()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getCategories success returns categories`() = runTest {
        coEvery { api.getCategories() } returns testCategories

        val result = quizRepository.getCategories()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getPopularQuizzes success returns quiz list`() = runTest {
        coEvery { api.getPopularQuizzes(10) } returns listOf(testQuiz)

        val result = quizRepository.getPopularQuizzes()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `getQuizzes passes params correctly`() = runTest {
        coEvery {
            api.getQuizzes(
                category = "science",
                difficulty = "hard",
                search = "phys",
                page = 2,
                limit = 10
            )
        } returns QuizListResponse(quizzes = listOf(testQuiz), total = 1, page = 2, limit = 10)

        val result = quizRepository.getQuizzes(
            category = "science",
            difficulty = "hard",
            search = "phys",
            page = 2,
            limit = 10
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.quizzes?.size)
    }

    @Test
    fun `searchQuizzes success returns quiz list`() = runTest {
        coEvery { api.searchQuizzes("science", 15, null, null) } returns listOf(testQuiz)

        val result = quizRepository.searchQuizzes("science")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Science Quiz", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `searchQuizzes failure returns error`() = runTest {
        coEvery { api.searchQuizzes(any(), any(), any(), any()) } throws Exception("Network")

        val result = quizRepository.searchQuizzes("test")

        assertTrue(result.isFailure)
    }

    @Test
    fun `searchQuizzes passes category and difficulty filters`() = runTest {
        coEvery {
            api.searchQuizzes("physics", 10, "science", "HARD")
        } returns listOf(testQuiz)

        val result = quizRepository.searchQuizzes(
            query = "physics",
            limit = 10,
            category = "science",
            difficulty = "HARD"
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `getQuizDetail success returns detail`() = runTest {
        val testDetail = QuizDetail(
            id = "q1",
            title = "Science Quiz",
            description = "A great quiz",
            category = testCategory,
            difficulty = "easy",
            questionCount = 10,
            timePerQuestion = 15,
            playCount = 500,
            thumbnailUrl = null
        )
        coEvery { api.getQuizDetail("q1") } returns testDetail

        val result = quizRepository.getQuizDetail("q1")

        assertTrue(result.isSuccess)
        assertEquals("A great quiz", result.getOrNull()?.description)
    }
}
