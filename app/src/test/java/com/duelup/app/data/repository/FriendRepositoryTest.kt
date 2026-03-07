package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Friend
import com.duelup.app.domain.model.FriendRequest
import com.duelup.app.domain.model.FriendRequestsResponse
import com.duelup.app.domain.model.FriendsListResponse
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
        Friend(friendshipId = "fs1", userId = "u1", username = "Alice", rating = 1300),
        Friend(friendshipId = "fs2", userId = "u2", username = "Bob", rating = 1100)
    )

    private val testPendingRequests = listOf(
        FriendRequest(
            friendshipId = "fs3", userId = "u3", username = "Charlie",
            rating = 1200, createdAt = "2024-01-01T00:00:00Z"
        )
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
    fun `getFriends success returns friends`() = runTest {
        coEvery { api.getFriends() } returns FriendsListResponse(testFriends, 2)

        val result = repository.getFriends()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.friends?.size)
    }

    @Test
    fun `getFriends failure returns error`() = runTest {
        coEvery { api.getFriends() } throws Exception("Network error")

        val result = repository.getFriends()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getReceivedRequests success returns requests`() = runTest {
        coEvery { api.getReceivedFriendRequests() } returns
                FriendRequestsResponse(testPendingRequests, 1)

        val result = repository.getReceivedRequests()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.requests?.size)
    }

    @Test
    fun `sendFriendRequest success returns request`() = runTest {
        val request = FriendRequest(
            friendshipId = "fs4", userId = "u4", username = "Dave",
            rating = 1000, createdAt = "2024-01-02T00:00:00Z"
        )
        coEvery { api.sendFriendRequest(any()) } returns request

        val result = repository.sendFriendRequest("u4")

        assertTrue(result.isSuccess)
        assertEquals("Dave", result.getOrNull()?.username)
    }

    @Test
    fun `acceptFriendRequest success`() = runTest {
        coEvery { api.acceptFriendRequest("fs3") } returns Unit

        val result = repository.acceptFriendRequest("fs3")

        assertTrue(result.isSuccess)
        coVerify { api.acceptFriendRequest("fs3") }
    }

    @Test
    fun `declineFriendRequest success`() = runTest {
        coEvery { api.declineFriendRequest("fs3") } returns Unit

        val result = repository.declineFriendRequest("fs3")

        assertTrue(result.isSuccess)
        coVerify { api.declineFriendRequest("fs3") }
    }

    @Test
    fun `removeFriend success`() = runTest {
        coEvery { api.removeFriend("fs1") } returns Unit

        val result = repository.removeFriend("fs1")

        assertTrue(result.isSuccess)
        coVerify { api.removeFriend("fs1") }
    }

    @Test
    fun `removeFriend failure returns error`() = runTest {
        coEvery { api.removeFriend("fs1") } throws Exception("Not found")

        val result = repository.removeFriend("fs1")

        assertTrue(result.isFailure)
    }
}
