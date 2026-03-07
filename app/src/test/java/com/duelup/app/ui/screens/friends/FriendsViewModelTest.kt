package com.duelup.app.ui.screens.friends

import com.duelup.app.data.repository.FriendRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FriendRepository

    private val testFriends = listOf(
        Friend(friendshipId = "fs1", userId = "u1", username = "Alice", rating = 1300),
        Friend(friendshipId = "fs2", userId = "u2", username = "Bob", rating = 1100)
    )

    private val testPendingRequest = FriendRequest(
        friendshipId = "fs3", userId = "u3", username = "Charlie",
        rating = 1200, createdAt = "2024-01-01T00:00:00Z"
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

    private fun setupDefaultMocks() {
        coEvery { repository.getFriends() } returns
                Result.success(FriendsListResponse(testFriends, 2))
        coEvery { repository.getReceivedRequests() } returns
                Result.success(FriendRequestsResponse(listOf(testPendingRequest), 1))
    }

    @Test
    fun `init loads friends and requests successfully`() = runTest {
        setupDefaultMocks()

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
        coEvery { repository.getReceivedRequests() } returns
                Result.success(FriendRequestsResponse(emptyList(), 0))

        val viewModel = FriendsViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `acceptRequest calls repository and reloads`() = runTest {
        setupDefaultMocks()
        coEvery { repository.acceptFriendRequest("fs3") } returns Result.success(Unit)

        val viewModel = FriendsViewModel(repository)
        viewModel.acceptRequest("fs3")

        coVerify { repository.acceptFriendRequest("fs3") }
        coVerify(exactly = 2) { repository.getFriends() }
    }

    @Test
    fun `declineRequest calls repository and reloads`() = runTest {
        setupDefaultMocks()
        coEvery { repository.declineFriendRequest("fs3") } returns Result.success(Unit)

        val viewModel = FriendsViewModel(repository)
        viewModel.declineRequest("fs3")

        coVerify { repository.declineFriendRequest("fs3") }
        coVerify(exactly = 2) { repository.getFriends() }
    }

    @Test
    fun `removeFriend calls repository and reloads`() = runTest {
        setupDefaultMocks()
        coEvery { repository.removeFriend("fs1") } returns Result.success(Unit)

        val viewModel = FriendsViewModel(repository)
        viewModel.removeFriend("fs1")

        coVerify { repository.removeFriend("fs1") }
        coVerify(exactly = 2) { repository.getFriends() }
    }

    @Test
    fun `acceptRequest failure does not reload`() = runTest {
        setupDefaultMocks()
        coEvery { repository.acceptFriendRequest("fs3") } returns
                Result.failure(Exception("Failed"))

        val viewModel = FriendsViewModel(repository)
        viewModel.acceptRequest("fs3")

        coVerify(exactly = 1) { repository.getFriends() }
    }
}
