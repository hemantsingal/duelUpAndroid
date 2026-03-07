package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val friendshipId: String,
    val userId: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val rating: Int = 1000,
    val since: String? = null
)

@Serializable
data class FriendRequest(
    val friendshipId: String,
    val userId: String,
    val username: String,
    val avatarUrl: String? = null,
    val rating: Int = 1000,
    val createdAt: String
)

@Serializable
data class FriendsListResponse(
    val friends: List<Friend> = emptyList(),
    val total: Int = 0
)

@Serializable
data class FriendRequestsResponse(
    val requests: List<FriendRequest> = emptyList(),
    val total: Int = 0
)

@Serializable
data class SendFriendRequestBody(
    val userId: String? = null,
    val username: String? = null
)
