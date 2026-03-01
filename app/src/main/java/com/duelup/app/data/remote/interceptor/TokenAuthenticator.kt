package com.duelup.app.data.remote.interceptor

import com.duelup.app.data.local.SessionManager
import com.duelup.app.data.remote.dto.RefreshTokenRequest
import com.duelup.app.domain.model.AuthResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val json: Json
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only retry once
        if (response.request.header("X-Retry-Auth") != null) {
            runBlocking { sessionManager.clearSession() }
            return null
        }

        return runBlocking {
            mutex.withLock {
                // Check if another thread already refreshed the token
                val currentToken = sessionManager.getAccessToken()
                val failedToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                if (currentToken != null && currentToken != failedToken) {
                    // Token was already refreshed by another request
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .header("X-Retry-Auth", "true")
                        .build()
                }

                // Attempt token refresh
                val refreshToken = sessionManager.getRefreshToken()
                if (refreshToken == null) {
                    sessionManager.clearSession()
                    return@runBlocking null
                }

                try {
                    val refreshResult = performTokenRefresh(refreshToken, response)
                    if (refreshResult != null) {
                        sessionManager.updateTokens(
                            refreshResult.accessToken,
                            refreshResult.refreshToken
                        )
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${refreshResult.accessToken}")
                            .header("X-Retry-Auth", "true")
                            .build()
                    } else {
                        sessionManager.clearSession()
                        null
                    }
                } catch (e: Exception) {
                    sessionManager.clearSession()
                    null
                }
            }
        }
    }

    private fun performTokenRefresh(refreshToken: String, originalResponse: Response): AuthResponse? {
        val baseUrl = originalResponse.request.url.newBuilder()
            .encodedPath("/api/v1/auth/refresh")
            .build()

        val requestBody = json.encodeToString(
            RefreshTokenRequest.serializer(),
            RefreshTokenRequest(refreshToken)
        )

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val client = OkHttpClient.Builder().build()
        val response = client.newCall(request).execute()

        return if (response.isSuccessful) {
            response.body?.string()?.let { body ->
                json.decodeFromString(AuthResponse.serializer(), body)
            }
        } else {
            null
        }
    }
}
