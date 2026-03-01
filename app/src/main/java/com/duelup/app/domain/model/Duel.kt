package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DuelPlayer(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
    val rating: Int,
    val isAI: Boolean = false
)

@Serializable
data class Duel(
    val id: String,
    val quizId: String,
    val quizTitle: String,
    val status: String,
    val player1: DuelPlayer,
    val player2: DuelPlayer? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val winnerId: String? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String = ""
)

@Serializable
data class DuelAnswer(
    val questionIndex: Int,
    val selectedOption: Int? = null,
    val isCorrect: Boolean,
    val responseTimeMs: Int,
    val pointsEarned: Int
)

@Serializable
data class DuelReplay(
    val duel: Duel,
    val player1Answers: List<DuelAnswer> = emptyList(),
    val player2Answers: List<DuelAnswer> = emptyList()
)
