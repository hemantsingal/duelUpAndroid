package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.ChallengeReward
import com.duelup.app.domain.model.ChallengesResponse
import com.duelup.app.domain.model.Challenge
import com.duelup.app.domain.model.DirectChallenge
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
class ChallengeRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
    private lateinit var repository: ChallengeRepository

    private val testChallenges = listOf(
        Challenge(
            id = "ch1", title = "Daily Duel", objective = "Win 3 duels today",
            currentProgress = 1, targetProgress = 3,
            reward = ChallengeReward(xp = 50),
            expiresAt = "2024-12-31T23:59:59Z", isCompleted = false
        )
    )

    private val testDirectChallenge = DirectChallenge(
        id = "dc1", challengerId = "u1", challengerUsername = "Player1",
        challengeeId = "u2", challengeeUsername = "Player2",
        quizId = "q1", quizTitle = "Science Quiz",
        status = "pending", createdAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        repository = ChallengeRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getChallenges success returns challenges`() = runTest {
        coEvery { api.getChallenges() } returns ChallengesResponse(testChallenges)

        val result = repository.getChallenges()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.challenges?.size)
    }

    @Test
    fun `getChallenges failure returns error`() = runTest {
        coEvery { api.getChallenges() } throws Exception("API error")

        val result = repository.getChallenges()

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createDirectChallenge success returns challenge`() = runTest {
        coEvery { api.createDirectChallenge(any()) } returns testDirectChallenge

        val result = repository.createDirectChallenge("u2", "q1")

        assertTrue(result.isSuccess)
        assertEquals("dc1", result.getOrNull()?.id)
        assertEquals("pending", result.getOrNull()?.status)
    }

    @Test
    fun `createDirectChallenge failure returns error`() = runTest {
        coEvery { api.createDirectChallenge(any()) } throws Exception("Failed")

        val result = repository.createDirectChallenge("u2", "q1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `acceptChallenge success returns updated challenge`() = runTest {
        val accepted = testDirectChallenge.copy(status = "accepted")
        coEvery { api.acceptChallenge("dc1") } returns accepted

        val result = repository.acceptChallenge("dc1")

        assertTrue(result.isSuccess)
        assertEquals("accepted", result.getOrNull()?.status)
    }

    @Test
    fun `declineChallenge success returns unit`() = runTest {
        coEvery { api.declineChallenge("dc1") } returns Unit

        val result = repository.declineChallenge("dc1")

        assertTrue(result.isSuccess)
        coVerify { api.declineChallenge("dc1") }
    }

    @Test
    fun `declineChallenge failure returns error`() = runTest {
        coEvery { api.declineChallenge("dc1") } throws Exception("Not found")

        val result = repository.declineChallenge("dc1")

        assertTrue(result.isFailure)
    }
}
