package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.data.remote.dto.UpdateProfileRequest
import com.duelup.app.domain.model.DuelHistoryResponse
import com.duelup.app.domain.model.LeaderboardResponse
import com.duelup.app.domain.model.UserProfile
import com.duelup.app.domain.model.UserStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getProfile(): Result<UserProfile> {
        return try {
            Result.success(api.getProfile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> {
        return try {
            Result.success(api.updateProfile(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<UserStats> {
        return try {
            Result.success(api.getStats())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDuelHistory(page: Int = 1, limit: Int = 20): Result<DuelHistoryResponse> {
        return try {
            Result.success(api.getDuelHistory(page, limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGlobalLeaderboard(limit: Int = 100): Result<LeaderboardResponse> {
        return try {
            Result.success(api.getGlobalLeaderboard(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyLeaderboard(limit: Int = 100): Result<LeaderboardResponse> {
        return try {
            Result.success(api.getWeeklyLeaderboard(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
