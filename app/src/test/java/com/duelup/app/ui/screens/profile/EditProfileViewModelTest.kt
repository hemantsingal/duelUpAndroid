package com.duelup.app.ui.screens.profile

import com.duelup.app.data.remote.dto.UpdateProfileRequest
import com.duelup.app.data.repository.UserRepository
import com.duelup.app.domain.model.UserProfile
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: UserRepository

    private val testProfile = UserProfile(
        id = "user1",
        username = "TestUser",
        displayName = "Test Display",
        avatarUrl = null,
        isGuest = true,
        rating = 1200
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads profile data into form fields`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)

        val state = viewModel.uiState.value
        assertEquals("TestUser", state.username)
        assertEquals("Test Display", state.displayName)
        assertFalse(state.isLoading)
    }

    @Test
    fun `updateUsername validates minimum length`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)

        viewModel.updateUsername("ab")
        assertNotNull(viewModel.uiState.value.usernameError)
        assertEquals("Username must be at least 3 characters", viewModel.uiState.value.usernameError)
    }

    @Test
    fun `updateUsername validates maximum length`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)

        viewModel.updateUsername("a".repeat(31))
        assertNotNull(viewModel.uiState.value.usernameError)
        assertEquals("Username must be at most 30 characters", viewModel.uiState.value.usernameError)
    }

    @Test
    fun `updateUsername clears error for valid name`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)

        viewModel.updateUsername("ab")
        assertNotNull(viewModel.uiState.value.usernameError)

        viewModel.updateUsername("ValidName")
        assertNull(viewModel.uiState.value.usernameError)
    }

    @Test
    fun `updateDisplayName truncates at 50 chars`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)

        val longName = "a".repeat(60)
        viewModel.updateDisplayName(longName)
        assertEquals(50, viewModel.uiState.value.displayName.length)
    }

    @Test
    fun `saveProfile succeeds and sets saveSuccess`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)
        coEvery { userRepository.updateProfile(any()) } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)
        viewModel.saveProfile()

        assertTrue(viewModel.uiState.value.saveSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveProfile failure sets error`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)
        coEvery { userRepository.updateProfile(any()) } returns
                Result.failure(Exception("Save failed"))

        val viewModel = EditProfileViewModel(userRepository)
        viewModel.saveProfile()

        assertFalse(viewModel.uiState.value.saveSuccess)
        assertEquals("Save failed", viewModel.uiState.value.error)
    }

    @Test
    fun `saveProfile does not save when username has error`() = runTest {
        coEvery { userRepository.getProfile() } returns Result.success(testProfile)

        val viewModel = EditProfileViewModel(userRepository)
        viewModel.updateUsername("ab") // too short
        viewModel.saveProfile()

        assertFalse(viewModel.uiState.value.isSaving)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
