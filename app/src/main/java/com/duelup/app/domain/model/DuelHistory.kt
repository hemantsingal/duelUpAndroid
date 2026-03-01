package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DuelHistoryItem(
    val id: String,
    val quizTitle: String,
    val opponentUsername: String,
    val opponentAvatarUrl: String? = null,
    val isAiOpponent: Boolean = false,
    val playerScore: Int,
    val opponentScore: Int,
    val result: String,
    val completedAt: String = ""
)

@Serializable
data class DuelHistoryResponse(
    val duels: List<DuelHistoryItem>,
    val total: Int,
    val page: Int,
    val limit: Int
)
