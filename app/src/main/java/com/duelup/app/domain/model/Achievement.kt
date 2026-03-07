package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String? = null,
    val category: String = "milestones",
    val pointsAwarded: Int = 0,
    val unlockedAt: String? = null,
    val isUnlocked: Boolean = false
)

@Serializable
data class AchievementsResponse(
    val achievements: List<Achievement> = emptyList()
)
