package com.duelup.app.data.repository

import com.duelup.app.data.local.SessionManager
import com.duelup.app.data.local.SessionState
import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.data.remote.dto.GuestLoginRequest
import com.duelup.app.domain.model.User
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: DuelUpApi,
    private val sessionManager: SessionManager
) {
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState

    suspend fun initialize() {
        sessionManager.initialize()
    }

    suspend fun guestLogin(deviceId: String): Result<User> {
        return try {
            val response = api.guestLogin(GuestLoginRequest(deviceId))
            sessionManager.saveSession(response)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            // Clear session even if API call fails
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    suspend fun getAccessToken(): String? = sessionManager.getAccessToken()
}
