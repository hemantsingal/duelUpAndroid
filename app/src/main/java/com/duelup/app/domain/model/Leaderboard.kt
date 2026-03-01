package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val avatarUrl: String? = null,
    val rating: Int,
    val wins: Int = 0,
    val totalGames: Int = 0,
    val winRate: Double = 0.0
)

@Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUser: LeaderboardEntry? = null
)
