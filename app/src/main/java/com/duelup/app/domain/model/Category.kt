package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val iconUrl: String? = null,
    val color: String? = null
)
