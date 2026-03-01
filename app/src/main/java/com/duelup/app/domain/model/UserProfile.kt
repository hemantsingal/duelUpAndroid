package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isGuest: Boolean,
    val rating: Int,
    val createdAt: String = "",
    val stats: UserStats? = null
)
