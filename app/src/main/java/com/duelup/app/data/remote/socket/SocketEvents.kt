package com.duelup.app.data.remote.socket

import kotlinx.serialization.Serializable

// --- Client -> Server Event Names ---
object ClientEvents {
    const val MATCHMAKING_JOIN = "matchmaking:join"
    const val MATCHMAKING_LEAVE = "matchmaking:leave"
    const val DUEL_READY = "duel:ready"
    const val DUEL_ANSWER = "duel:answer"
    const val DUEL_RECONNECT = "duel:reconnect"
    const val PING = "ping"
}

// --- Server -> Client Event Names ---
object ServerEvents {
    const val CONNECTION_ESTABLISHED = "connection:established"
    const val MATCHMAKING_SEARCHING = "matchmaking:searching"
    const val MATCHMAKING_FOUND = "matchmaking:found"
    const val MATCHMAKING_LEFT = "matchmaking:left"
    const val DUEL_START = "duel:start"
    const val NEXT_QUESTION = "duel:next-question"
    const val OPPONENT_PROGRESS = "duel:opponent-progress"
    const val QUESTION_RESULT = "duel:question-result"
    const val DUEL_END = "duel:end"
    const val DUEL_STATE_SYNC = "duel:state-sync"
    const val DUEL_RECONNECT_FAILED = "duel:reconnect-failed"
    const val ERROR = "error"
    const val PONG = "pong"
}

// --- Client -> Server Payloads ---
@Serializable
data class JoinMatchmakingPayload(
    val quizId: String,
    val preferredMode: String = "any"
)

@Serializable
data class DuelReadyPayload(val duelId: String)

@Serializable
data class SubmitAnswerPayload(
    val duelId: String,
    val questionIndex: Int,
    val answerIndex: Int,
    val timestamp: Long
)

@Serializable
data class DuelReconnectPayload(val duelId: String)

// --- Server -> Client Payloads ---
@Serializable
data class ConnectionEstablishedPayload(
    val userId: String,
    val serverTime: Long
)

@Serializable
data class MatchmakingSearchingPayload(
    val quizId: String,
    val queuePosition: Int = 0,
    val estimatedWait: Int = 0
)

@Serializable
data class MatchOpponentInfo(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
    val rating: Int
)

@Serializable
data class MatchQuizInfo(
    val id: String,
    val title: String,
    val questionCount: Int,
    val timePerQuestion: Int
)

@Serializable
data class MatchmakingFoundPayload(
    val duelId: String,
    val opponent: MatchOpponentInfo,
    val quiz: MatchQuizInfo
)

@Serializable
data class QuestionPayload(
    val index: Int,
    val text: String,
    val options: List<String>,
    val timeLimit: Int,
    val imageUrl: String? = null
)

@Serializable
data class DuelStartPayload(
    val duelId: String,
    val question: QuestionPayload,
    val startTime: Long
)

@Serializable
data class NextQuestionPayload(
    val question: QuestionPayload,
    val startTime: Long
)

@Serializable
data class OpponentProgressPayload(
    val questionIndex: Int,
    val hasAnswered: Boolean,
    val correct: Boolean = false,
    val totalScore: Int = 0,
    val timeRemaining: Int = 0
)

@Serializable
data class PlayerResultInfo(
    val answer: Int,
    val isCorrect: Boolean,
    val timeMs: Int,
    val pointsEarned: Int,
    val totalScore: Int
)

@Serializable
data class QuestionResultPayload(
    val questionIndex: Int,
    val correctAnswer: Int,
    val player: PlayerResultInfo,
    val opponent: PlayerResultInfo
)

@Serializable
data class DuelEndPlayerInfo(
    val finalScore: Int,
    val correctAnswers: Int,
    val avgTimeMs: Int,
    val ratingChange: Int = 0
)

@Serializable
data class DuelEndOpponentInfo(
    val finalScore: Int,
    val correctAnswers: Int,
    val avgTimeMs: Int
)

@Serializable
data class DuelRewards(
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0,
    val achievements: List<String> = emptyList()
)

@Serializable
data class DuelEndPayload(
    val duelId: String,
    val result: String, // "win", "lose", "draw"
    val player: DuelEndPlayerInfo,
    val opponent: DuelEndOpponentInfo,
    val rewards: DuelRewards? = null
)

@Serializable
data class ErrorPayload(
    val code: String = "",
    val message: String = ""
)
