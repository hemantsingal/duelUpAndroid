package com.duelup.app.data.remote.api

import com.duelup.app.data.remote.dto.GuestLoginRequest
import com.duelup.app.data.remote.dto.LinkAccountRequest
import com.duelup.app.data.remote.dto.RefreshTokenRequest
import com.duelup.app.data.remote.dto.UpdateProfileRequest
import com.duelup.app.domain.model.Achievement
import com.duelup.app.domain.model.AuthResponse
import com.duelup.app.domain.model.Category
import com.duelup.app.domain.model.ChallengesResponse
import com.duelup.app.domain.model.DirectChallenge
import com.duelup.app.domain.model.DirectChallengeRequest
import com.duelup.app.domain.model.Duel
import com.duelup.app.domain.model.DuelHistoryResponse
import com.duelup.app.domain.model.DuelReplay
import com.duelup.app.domain.model.FriendRequestsResponse
import com.duelup.app.domain.model.FriendsListResponse
import com.duelup.app.domain.model.LeaderboardResponse
import com.duelup.app.domain.model.Quiz
import com.duelup.app.domain.model.QuizDetail
import com.duelup.app.domain.model.QuizListResponse
import com.duelup.app.domain.model.SendFriendRequestBody
import com.duelup.app.domain.model.UserProfile
import com.duelup.app.domain.model.UserStats
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DuelUpApi {

    // Auth
    @POST("auth/guest")
    suspend fun guestLogin(@Body body: GuestLoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): AuthResponse

    @POST("auth/link-account")
    suspend fun linkAccount(@Body body: LinkAccountRequest): AuthResponse

    @DELETE("auth/logout")
    suspend fun logout()

    // Users
    @GET("users/me")
    suspend fun getProfile(): UserProfile

    @POST("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserProfile

    @GET("users/me/stats")
    suspend fun getStats(): UserStats

    @GET("users/me/history")
    suspend fun getDuelHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): DuelHistoryResponse

    @GET("users/{id}/profile")
    suspend fun getPublicProfile(@Path("id") userId: String): UserProfile

    // Quizzes
    @GET("quizzes")
    suspend fun getQuizzes(
        @Query("category") category: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("search") search: String? = null,
        @Query("featured") featured: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): QuizListResponse

    @GET("quizzes/categories")
    suspend fun getCategories(): List<Category>

    @GET("quizzes/popular")
    suspend fun getPopularQuizzes(@Query("limit") limit: Int = 10): List<Quiz>

    @GET("quizzes/{id}")
    suspend fun getQuizDetail(@Path("id") quizId: String): QuizDetail

    // Duels
    @GET("duels/{id}")
    suspend fun getDuel(@Path("id") duelId: String): Duel

    @GET("duels/{id}/replay")
    suspend fun getDuelReplay(@Path("id") duelId: String): DuelReplay

    // Leaderboard
    @GET("leaderboard/global")
    suspend fun getGlobalLeaderboard(@Query("limit") limit: Int = 100): LeaderboardResponse

    @GET("leaderboard/weekly")
    suspend fun getWeeklyLeaderboard(@Query("limit") limit: Int = 100): LeaderboardResponse

    // Achievements
    @GET("users/me/achievements")
    suspend fun getAchievements(): List<Achievement>

    // Challenges
    @GET("challenges")
    suspend fun getChallenges(): ChallengesResponse

    @POST("challenges/direct")
    suspend fun createDirectChallenge(@Body body: DirectChallengeRequest): DirectChallenge

    @POST("challenges/{id}/accept")
    suspend fun acceptChallenge(@Path("id") challengeId: String): DirectChallenge

    @DELETE("challenges/{id}/decline")
    suspend fun declineChallenge(@Path("id") challengeId: String)

    // Friends
    @GET("friends")
    suspend fun getFriends(): FriendsListResponse

    @GET("friends/requests/received")
    suspend fun getReceivedFriendRequests(): FriendRequestsResponse

    @GET("friends/requests/sent")
    suspend fun getSentFriendRequests(): FriendRequestsResponse

    @POST("friends/request")
    suspend fun sendFriendRequest(@Body body: SendFriendRequestBody): com.duelup.app.domain.model.FriendRequest

    @POST("friends/{friendshipId}/accept")
    suspend fun acceptFriendRequest(@Path("friendshipId") friendshipId: String)

    @POST("friends/{friendshipId}/decline")
    suspend fun declineFriendRequest(@Path("friendshipId") friendshipId: String)

    @DELETE("friends/{friendshipId}")
    suspend fun removeFriend(@Path("friendshipId") friendshipId: String)
}
