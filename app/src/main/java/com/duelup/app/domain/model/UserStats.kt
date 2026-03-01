package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val totalDuels: Int = 0,
    val duelsWon: Int = 0,
    val duelsLost: Int = 0,
    val duelsDraw: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Double = 0.0,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val avgResponseTimeMs: Int = 0
)
