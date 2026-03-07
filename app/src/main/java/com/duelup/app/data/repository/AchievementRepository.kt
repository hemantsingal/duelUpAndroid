package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Achievement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getAchievements(): Result<List<Achievement>> {
        return try {
            Result.success(api.getAchievements())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
