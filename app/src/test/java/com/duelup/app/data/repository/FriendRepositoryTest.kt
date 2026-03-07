package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Friend
import com.duelup.app.domain.model.FriendRequest
import com.duelup.app.domain.model.FriendsResponse
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
class FriendRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
    private lateinit var repository: FriendRepository

    private val testFriends = listOf(
        Friend(
            id = "f1", username = "Alice", rating = 1300, isOnline = true
        ),
        Friend(
            id = "f2", username = "Bob", rating = 1100, isOnline = false
        )
    )

    private val testPendingRequests = listOf(
        FriendRequest(
            id = "fr1", fromUserId = "u3", fromUsername = "Charlie",
            toUserId = "me", status = "pending", createdAt = "2024-01-01T00:00:00Z"
        )
    )

    private val testFriendsResponse = FriendsResponse(
        friends = testFriends,
        pendingRequests = testPendingRequests,
        totalFriends = 2
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        repository = FriendRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFriends success returns friends and requests`() = runTest {
        coEvery { api.getFriends() } returns testFriendsResponse

        val result = repository.getFriends()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.friends?.size)
        assertEquals(1, result.getOrNull()?.pendingRequests?.size)
    }

    @Test
    fun `getFriends failure returns error`() = runTest {
        coEvery { api.getFriends() } throws Exception("Network error")

        val result = repository.getFriends()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `sendFriendRequest success returns request`() = runTest {
        val request = FriendRequest(
            id = "fr2", fromUserId = "me", fromUsername = "Me",
            toUserId = "u4", status = "pending", createdAt = "2024-01-02T00:00:00Z"
        )
        coEvery { api.sendFriendRequest("u4") } returns request

        val result = repository.sendFriendRequest("u4")

        assertTrue(result.isSuccess)
        assertEquals("u4", result.getOrNull()?.toUserId)
    }

    @Test
    fun `acceptFriendRequest success returns friend`() = runTest {
        val friend = Friend(id = "u3", username = "Charlie", rating = 1200)
        coEvery { api.acceptFriendRequest("u3") } returns friend

        val result = repository.acceptFriendRequest("u3")

        assertTrue(result.isSuccess)
        assertEquals("Charlie", result.getOrNull()?.username)
    }

    @Test
    fun `removeFriend success`() = runTest {
        coEvery { api.removeFriend("f1") } returns Unit

        val result = repository.removeFriend("f1")

        assertTrue(result.isSuccess)
        coVerify { api.removeFriend("f1") }
    }

    @Test
    fun `removeFriend failure returns error`() = runTest {
        coEvery { api.removeFriend("f1") } throws Exception("Not found")

        val result = repository.removeFriend("f1")

        assertTrue(result.isFailure)
    }
}
