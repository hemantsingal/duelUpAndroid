package com.duelup.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GuestLoginRequest(
    val deviceId: String,
    val platform: String = "android"
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class LinkAccountRequest(
    val supabaseToken: String,
    val guestUserId: String
)

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)
