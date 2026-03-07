package com.duelup.app.data.remote.interceptor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {

    private val _rateLimited = MutableStateFlow(false)
    val rateLimited: StateFlow<Boolean> = _rateLimited.asStateFlow()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 429) {
            _rateLimited.value = true
        } else {
            _rateLimited.value = false
        }

        return response
    }
}
