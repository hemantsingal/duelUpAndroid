package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val key: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val category: String = "milestones",
    val unlockedAt: String? = null
)
