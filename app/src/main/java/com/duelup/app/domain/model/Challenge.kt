package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String,
    val title: String,
    val objective: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val reward: ChallengeReward,
    val expiresAt: String,
    val isCompleted: Boolean = false,
    val completedAt: String? = null
)

@Serializable
data class ChallengeReward(
    val xp: Int = 0,
    val achievementId: String? = null
)

@Serializable
data class ChallengesResponse(
    val challenges: List<Challenge> = emptyList()
)

@Serializable
data class DirectChallenge(
    val id: String,
    val challengerId: String,
    val challengerUsername: String,
    val challengerAvatar: String? = null,
    val challengeeId: String,
    val challengeeUsername: String,
    val challengeeAvatar: String? = null,
    val quizId: String,
    val quizTitle: String,
    val status: String,
    val createdAt: String,
    val acceptedAt: String? = null,
    val completedAt: String? = null
)

@Serializable
data class DirectChallengeRequest(
    val challengeeId: String,
    val quizId: String
)
