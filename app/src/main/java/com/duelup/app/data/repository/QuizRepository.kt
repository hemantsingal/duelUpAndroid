package com.duelup.app.data.repository

import com.duelup.app.data.remote.api.DuelUpApi
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.Quiz
import com.duelup.app.domain.model.QuizDetail
import com.duelup.app.domain.model.QuizListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val api: DuelUpApi
) {
    suspend fun getFeaturedQuizzes(limit: Int = 5): Result<List<Quiz>> {
        return try {
            val response = api.getQuizzes(featured = true, limit = limit)
            Result.success(response.quizzes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            Result.success(api.getCategories())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularQuizzes(limit: Int = 10): Result<List<Quiz>> {
        return try {
            Result.success(api.getPopularQuizzes(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRandomQuizzes(count: Int = 6): Result<List<Quiz>> {
        return try {
            val response = api.getQuizzes(random = count)
            Result.success(response.quizzes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizzes(
        category: String? = null,
        difficulty: String? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<QuizListResponse> {
        return try {
            Result.success(api.getQuizzes(category, difficulty, search, page = page, limit = limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizDetail(quizId: String): Result<QuizDetail> {
        return try {
            Result.success(api.getQuizDetail(quizId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchQuizzes(
        query: String,
        limit: Int = 15,
        category: String? = null,
        difficulty: String? = null
    ): Result<List<Quiz>> {
        return try {
            Result.success(api.searchQuizzes(query, limit, category, difficulty))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
