package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.ChallengesResponse
import com.duelup.app.domain.model.DirectChallenge
import com.duelup.app.domain.model.DirectChallengeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getChallenges(): Result<ChallengesResponse> {
        return try {
            Result.success(api.getChallenges())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDirectChallenge(challengeeId: String, quizId: String): Result<DirectChallenge> {
        return try {
            Result.success(api.createDirectChallenge(DirectChallengeRequest(challengeeId, quizId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptChallenge(challengeId: String): Result<DirectChallenge> {
        return try {
            Result.success(api.acceptChallenge(challengeId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineChallenge(challengeId: String): Result<Unit> {
        return try {
            api.declineChallenge(challengeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
