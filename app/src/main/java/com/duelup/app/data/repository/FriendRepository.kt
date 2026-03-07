package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Friend
import com.duelup.app.domain.model.FriendRequest
import com.duelup.app.domain.model.FriendsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getFriends(): Result<FriendsResponse> {
        return try {
            Result.success(api.getFriends())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(userId: String): Result<FriendRequest> {
        return try {
            Result.success(api.sendFriendRequest(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(userId: String): Result<Friend> {
        return try {
            Result.success(api.acceptFriendRequest(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(userId: String): Result<Unit> {
        return try {
            api.removeFriend(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
