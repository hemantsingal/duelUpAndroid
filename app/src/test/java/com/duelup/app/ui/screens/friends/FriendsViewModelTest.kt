package com.duelup.app.ui.screens.friends

import com.duelup.app.data.repository.FriendRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FriendRepository

    private val onlineFriend = Friend(
        id = "f1", username = "Alice", rating = 1300, isOnline = true
    )

    private val offlineFriend = Friend(
        id = "f2", username = "Bob", rating = 1100, isOnline = false
    )

    private val testPendingRequest = FriendRequest(
        id = "fr1", fromUserId = "u3", fromUsername = "Charlie",
        toUserId = "me", status = "pending", createdAt = "2024-01-01T00:00:00Z"
    )

    private val testFriendsResponse = FriendsResponse(
        friends = listOf(onlineFriend, offlineFriend),
        pendingRequests = listOf(testPendingRequest),
        totalFriends = 2
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads friends successfully`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)

        val viewModel = FriendsViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(2, state.friends.size)
        assertEquals(1, state.pendingRequests.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init loads friends failure sets error`() = runTest {
        coEvery { repository.getFriends() } returns
                Result.failure(Exception("Network error"))

        val viewModel = FriendsViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `onlineFriends filters correctly`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)

        val viewModel = FriendsViewModel(repository)

        val online = viewModel.uiState.value.onlineFriends
        assertEquals(1, online.size)
        assertEquals("Alice", online.first().username)
    }

    @Test
    fun `offlineFriends filters correctly`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)

        val viewModel = FriendsViewModel(repository)

        val offline = viewModel.uiState.value.offlineFriends
        assertEquals(1, offline.size)
        assertEquals("Bob", offline.first().username)
    }

    @Test
    fun `acceptRequest calls repository and reloads`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)
        coEvery { repository.acceptFriendRequest("u3") } returns
                Result.success(Friend(id = "u3", username = "Charlie", rating = 1200))

        val viewModel = FriendsViewModel(repository)
        viewModel.acceptRequest("u3")

        coVerify { repository.acceptFriendRequest("u3") }
        // getFriends called twice: init + reload after accept
        coVerify(exactly = 2) { repository.getFriends() }
    }

    @Test
    fun `removeFriend calls repository and reloads`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)
        coEvery { repository.removeFriend("f1") } returns Result.success(Unit)

        val viewModel = FriendsViewModel(repository)
        viewModel.removeFriend("f1")

        coVerify { repository.removeFriend("f1") }
        coVerify(exactly = 2) { repository.getFriends() }
    }

    @Test
    fun `acceptRequest failure does not reload`() = runTest {
        coEvery { repository.getFriends() } returns Result.success(testFriendsResponse)
        coEvery { repository.acceptFriendRequest("u3") } returns
                Result.failure(Exception("Failed"))

        val viewModel = FriendsViewModel(repository)
        viewModel.acceptRequest("u3")

        // getFriends only called once during init, not reloaded on failure
        coVerify(exactly = 1) { repository.getFriends() }
    }
}
