package com.duelup.app.data.repository

import com.duelup.app.data.local.SessionManager
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.data.remote.dto.GuestLoginRequest
import com.duelup.app.domain.model.AuthResponse
import com.duelup.app.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class AuthRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var api: DuelUpApi
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    private val testUser = User(
        id = "user1",
        username = "guest_abc",
        displayName = null,
        avatarUrl = null,
        isGuest = true,
        rating = 1000
    )

    private val testAuthResponse = AuthResponse(
        accessToken = "access_token_123",
        refreshToken = "refresh_token_456",
        user = testUser
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        api = mockk()
        sessionManager = mockk(relaxed = true)
        every { sessionManager.sessionState } returns MutableStateFlow(SessionState.Unauthenticated)
        authRepository = AuthRepository(api, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `guestLogin success saves session and returns user`() = runTest {
        coEvery { api.guestLogin(any()) } returns testAuthResponse

        val result = authRepository.guestLogin("device123")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
        coVerify { sessionManager.saveSession(testAuthResponse) }
    }

    @Test
    fun `guestLogin failure returns error`() = runTest {
        coEvery { api.guestLogin(any()) } throws Exception("API error")

        val result = authRepository.guestLogin("device123")

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears session even on API failure`() = runTest {
        coEvery { api.logout() } throws Exception("Network error")

        val result = authRepository.logout()

        assertTrue(result.isSuccess)
        coVerify { sessionManager.clearSession() }
    }

    @Test
    fun `logout success clears session`() = runTest {
        coEvery { api.logout() } returns Unit

        val result = authRepository.logout()

        assertTrue(result.isSuccess)
        coVerify { sessionManager.clearSession() }
    }

    @Test
    fun `initialize delegates to session manager`() = runTest {
        coEvery { sessionManager.initialize() } returns Unit

        authRepository.initialize()

        coVerify { sessionManager.initialize() }
    }

    @Test
    fun `getAccessToken delegates to session manager`() = runTest {
        coEvery { sessionManager.getAccessToken() } returns "token123"

        val token = authRepository.getAccessToken()

        assertEquals("token123", token)
    }
}
