package com.duelup.app.data.remote.interceptor

import com.duelup.app.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    private val publicPaths = listOf("auth/guest", "auth/refresh")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Skip auth header for public endpoints
        if (publicPaths.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking { sessionManager.getAccessToken() }

        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
