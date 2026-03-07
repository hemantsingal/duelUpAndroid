package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.FriendRequest
import com.duelup.app.domain.model.FriendRequestsResponse
import com.duelup.app.domain.model.FriendsListResponse
import com.duelup.app.domain.model.SendFriendRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getFriends(): Result<FriendsListResponse> {
        return try {
            Result.success(api.getFriends())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedRequests(): Result<FriendRequestsResponse> {
        return try {
            Result.success(api.getReceivedFriendRequests())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(userId: String): Result<FriendRequest> {
        return try {
            Result.success(api.sendFriendRequest(SendFriendRequestBody(userId = userId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            api.acceptFriendRequest(friendshipId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            api.declineFriendRequest(friendshipId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendshipId: String): Result<Unit> {
        return try {
            api.removeFriend(friendshipId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
