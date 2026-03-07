package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
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
        api = mockk()
        repository = AchievementRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAchievements success returns achievements`() = runTest {
        coEvery { api.getAchievements() } returns testAchievements

        val result = repository.getAchievements()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("First Win", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `getAchievements failure returns error`() = runTest {
        coEvery { api.getAchievements() } throws Exception("Network error")

        val result = repository.getAchievements()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
