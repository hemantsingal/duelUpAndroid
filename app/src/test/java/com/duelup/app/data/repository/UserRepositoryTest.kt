package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.data.remote.dto.UpdateProfileRequest
import com.duelup.app.domain.model.DuelHistoryItem
import com.duelup.app.domain.model.DuelHistoryResponse
import com.duelup.app.domain.model.LeaderboardEntry
import com.duelup.app.domain.model.LeaderboardResponse
import com.duelup.app.domain.model.UserProfile
import com.duelup.app.domain.model.UserStats
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
class UserRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
    private lateinit var userRepository: UserRepository

    private val testProfile = UserProfile(
        id = "u1", username = "test", displayName = "Test",
        isGuest = false, rating = 1300
    )

    private val testStats = UserStats(totalDuels = 20, duelsWon = 12)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        userRepository = UserRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfile success`() = runTest {
        coEvery { api.getProfile() } returns testProfile

        val result = userRepository.getProfile()

        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.username)
    }

    @Test
    fun `getProfile failure`() = runTest {
        coEvery { api.getProfile() } throws Exception("Unauthorized")

        val result = userRepository.getProfile()

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateProfile calls API with request`() = runTest {
        val request = UpdateProfileRequest(username = "newname", displayName = "New Name")
        coEvery { api.updateProfile(request) } returns testProfile.copy(username = "newname")

        val result = userRepository.updateProfile(request)

        assertTrue(result.isSuccess)
        assertEquals("newname", result.getOrNull()?.username)
        coVerify { api.updateProfile(request) }
    }

    @Test
    fun `getStats success`() = runTest {
        coEvery { api.getStats() } returns testStats

        val result = userRepository.getStats()

        assertTrue(result.isSuccess)
        assertEquals(20, result.getOrNull()?.totalDuels)
    }

    @Test
    fun `getDuelHistory passes pagination params`() = runTest {
        val response = DuelHistoryResponse(
            duels = listOf(
                DuelHistoryItem(
                    id = "d1", quizTitle = "Quiz", opponentUsername = "Opp",
                    playerScore = 500, opponentScore = 300, result = "win"
                )
            ),
            total = 1, page = 2, limit = 10
        )
        coEvery { api.getDuelHistory(2, 10) } returns response

        val result = userRepository.getDuelHistory(page = 2, limit = 10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.duels?.size)
    }

    @Test
    fun `getGlobalLeaderboard success`() = runTest {
        val response = LeaderboardResponse(
            entries = listOf(
                LeaderboardEntry(
                    rank = 1, userId = "u1", username = "Top", rating = 2000
                )
            )
        )
        coEvery { api.getGlobalLeaderboard(100) } returns response

        val result = userRepository.getGlobalLeaderboard()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.entries?.size)
    }

    @Test
    fun `getWeeklyLeaderboard success`() = runTest {
        val response = LeaderboardResponse(entries = emptyList())
        coEvery { api.getWeeklyLeaderboard(100) } returns response

        val result = userRepository.getWeeklyLeaderboard()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.entries?.isEmpty() == true)
    }
}
