package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Duel
import com.duelup.app.domain.model.DuelReplay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuelRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getDuel(duelId: String): Result<Duel> {
        return try {
            Result.success(api.getDuel(duelId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDuelReplay(duelId: String): Result<DuelReplay> {
        return try {
            Result.success(api.getDuelReplay(duelId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
