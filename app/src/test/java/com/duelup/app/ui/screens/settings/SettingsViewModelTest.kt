package com.duelup.app.ui.screens.settings

import app.cash.turbine.test
import com.duelup.app.data.local.UserPreferences
import com.duelup.app.data.repository.AuthRepository
import com.duelup.app.util.SoundManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var soundManager: SoundManager

    private val soundEnabledFlow = MutableStateFlow(true)
    private val hapticEnabledFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        soundManager = mockk(relaxed = true)

        every { userPreferences.soundEnabled } returns soundEnabledFlow
        every { userPreferences.hapticEnabled } returns hapticEnabledFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(userPreferences, authRepository, soundManager)

    @Test
    fun `init collects preferences into ui state`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.soundEnabled)
        assertTrue(viewModel.uiState.value.hapticEnabled)
    }

    @Test
    fun `preference changes update ui state`() = runTest {
        val viewModel = createViewModel()

        soundEnabledFlow.value = false

        assertFalse(viewModel.uiState.value.soundEnabled)
        assertTrue(viewModel.uiState.value.hapticEnabled)
    }

    @Test
    fun `setSoundEnabled updates preferences and sound manager`() = runTest {
        val viewModel = createViewModel()

        viewModel.setSoundEnabled(false)

        coVerify { userPreferences.setSoundEnabled(false) }
        verify { soundManager.setEnabled(false) }
    }

    @Test
    fun `setHapticEnabled updates preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.setHapticEnabled(false)

        coVerify { userPreferences.setHapticEnabled(false) }
    }

    @Test
    fun `logout calls auth repository and emits event`() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)
        val viewModel = createViewModel()

        viewModel.logoutEvent.test {
            viewModel.logout()
            awaitItem()
        }

        coVerify { authRepository.logout() }
    }
}
