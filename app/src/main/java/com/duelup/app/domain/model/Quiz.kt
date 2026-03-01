package com.duelup.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: String,
    val questionCount: Int,
    val timePerQuestion: Int,
    val playCount: Int,
    val category: Category? = null
)

@Serializable
data class QuizDetail(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: String,
    val questionCount: Int,
    val timePerQuestion: Int,
    val playCount: Int,
    val category: Category? = null,
    val isFeatured: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class QuizListResponse(
    val quizzes: List<Quiz>,
    val total: Int,
    val page: Int,
    val limit: Int
)
