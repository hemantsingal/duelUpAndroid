package com.duelup.app.ui.screens.profile

import app.cash.turbine.test
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.data.repository.UserRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository

    private val testProfile = UserProfile(
        id = "user1",
        username = "TestUser",
        displayName = "Test Display",
        avatarUrl = null,
        isGuest = true,
        rating = 1200,
        createdAt = "2024-01-01T00:00:00",
        stats = UserStats(totalDuels = 10, duelsWon = 5)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads profile successfully`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = ProfileViewModel(userRepository, authRepository)

        val state = viewModel.uiState.value
        assertNotNull(state.profile)
        assertEquals("TestUser", state.profile!!.username)
        assertEquals(1200, state.profile!!.rating)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init load failure sets error`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.failure(Exception("Network error"))

        val viewModel = ProfileViewModel(userRepository, authRepository)

        val state = viewModel.uiState.value
        assertNull(state.profile)
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `logout emits event and calls repository`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)
        coEvery { authRepository.logout() } returns Result.success(Unit)

        val viewModel = ProfileViewModel(userRepository, authRepository)

        viewModel.logoutEvent.test {
            viewModel.logout()
            awaitItem()
            coVerify { authRepository.logout() }
        }
    }

    @Test
    fun `loadProfile retries after error`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.failure(Exception("Error"))

        val viewModel = ProfileViewModel(userRepository, authRepository)
        assertEquals("Error", viewModel.uiState.value.error)

        coEvery { userRepository.getProfile() } returns Result.success(testProfile)
        viewModel.loadProfile()

        assertNotNull(viewModel.uiState.value.profile)
        assertNull(viewModel.uiState.value.error)
    }
}
