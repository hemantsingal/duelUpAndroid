package com.duelup.app.data.remote.socket

import com.duelup.app.data.local.SessionManager
import com.duelup.app.util.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val json: Json
) {
    private var socket: Socket? = null

    fun connect() {
        if (socket?.connected() == true) return

        val token = runBlocking { sessionManager.getAccessToken() } ?: return

        val options = IO.Options().apply {
            forceNew = true
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
            auth = mapOf("token" to token)
        }

        socket = IO.socket("${Constants.SOCKET_URL}${Constants.SOCKET_NAMESPACE}", options)
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun isConnected(): Boolean = socket?.connected() == true

    // --- Emit events ---
    fun joinMatchmaking(quizId: String) {
        val payload = JSONObject().apply {
            put("quizId", quizId)
            put("preferredMode", "any")
        }
        socket?.emit(ClientEvents.MATCHMAKING_JOIN, payload)
    }

    fun leaveMatchmaking() {
        socket?.emit(ClientEvents.MATCHMAKING_LEAVE)
    }

    fun sendReady(duelId: String) {
        val payload = JSONObject().apply { put("duelId", duelId) }
        socket?.emit(ClientEvents.DUEL_READY, payload)
    }

    fun sendAnswer(duelId: String, questionIndex: Int, answerIndex: Int) {
        val payload = JSONObject().apply {
            put("duelId", duelId)
            put("questionIndex", questionIndex)
            put("answerIndex", answerIndex)
            put("timestamp", System.currentTimeMillis())
        }
        socket?.emit(ClientEvents.DUEL_ANSWER, payload)
    }

    fun sendAnswerTimeout(duelId: String, questionIndex: Int) {
        val payload = JSONObject().apply {
            put("duelId", duelId)
            put("questionIndex", questionIndex)
            put("answerIndex", -1)
            put("timestamp", System.currentTimeMillis())
        }
        socket?.emit(ClientEvents.DUEL_ANSWER, payload)
    }

    fun sendReconnect(duelId: String) {
        val payload = JSONObject().apply { put("duelId", duelId) }
        socket?.emit(ClientEvents.DUEL_RECONNECT, payload)
    }

    fun sendPing() {
        socket?.emit(ClientEvents.PING)
    }

    // --- Listen for events (as Flows) ---
    fun <T> onEvent(eventName: String, deserialize: (JSONObject) -> T): Flow<T> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            try {
                val data = args.firstOrNull()
                if (data is JSONObject) {
                    trySend(deserialize(data))
                }
            } catch (e: Exception) {
                // Log parse error silently
            }
        }
        socket?.on(eventName, listener)
        awaitClose {
            socket?.off(eventName, listener)
        }
    }

    fun onMatchmakingSearching(): Flow<MatchmakingSearchingPayload> =
        onEvent(ServerEvents.MATCHMAKING_SEARCHING) {
            json.decodeFromString(MatchmakingSearchingPayload.serializer(), it.toString())
        }

    fun onMatchmakingFound(): Flow<MatchmakingFoundPayload> =
        onEvent(ServerEvents.MATCHMAKING_FOUND) {
            json.decodeFromString(MatchmakingFoundPayload.serializer(), it.toString())
        }

    fun onDuelStart(): Flow<DuelStartPayload> =
        onEvent(ServerEvents.DUEL_START) {
            json.decodeFromString(DuelStartPayload.serializer(), it.toString())
        }

    fun onNextQuestion(): Flow<NextQuestionPayload> =
        onEvent(ServerEvents.NEXT_QUESTION) {
            json.decodeFromString(NextQuestionPayload.serializer(), it.toString())
        }

    fun onOpponentProgress(): Flow<OpponentProgressPayload> =
        onEvent(ServerEvents.OPPONENT_PROGRESS) {
            json.decodeFromString(OpponentProgressPayload.serializer(), it.toString())
        }

    fun onQuestionResult(): Flow<QuestionResultPayload> =
        onEvent(ServerEvents.QUESTION_RESULT) {
            json.decodeFromString(QuestionResultPayload.serializer(), it.toString())
        }

    fun onDuelEnd(): Flow<DuelEndPayload> =
        onEvent(ServerEvents.DUEL_END) {
            json.decodeFromString(DuelEndPayload.serializer(), it.toString())
        }

    fun onError(): Flow<ErrorPayload> =
        onEvent(ServerEvents.ERROR) {
            json.decodeFromString(ErrorPayload.serializer(), it.toString())
        }

    fun onDisconnect(): Flow<Unit> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { trySend(Unit) }
        socket?.on(Socket.EVENT_DISCONNECT, listener)
        awaitClose { socket?.off(Socket.EVENT_DISCONNECT, listener) }
    }
}
