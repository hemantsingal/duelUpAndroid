package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val rating: Int = 1000,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val friendsSince: String? = null
)

@Serializable
data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val fromUsername: String,
    val fromAvatar: String? = null,
    val toUserId: String,
    val status: String = "pending",
    val createdAt: String
)

@Serializable
data class FriendsResponse(
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val totalFriends: Int = 0
)
