package com.duelup.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.duelup.app.domain.model.AuthResponse
import com.duelup.app.domain.model.User
import com.duelup.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed class SessionState {
    data object Loading : SessionState()
    data class Authenticated(val user: User) : SessionState()
    data object Unauthenticated : SessionState()
}

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) {
    private val accessTokenKey = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val refreshTokenKey = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val userJsonKey = stringPreferencesKey(Constants.KEY_USER_JSON)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    suspend fun initialize() {
        val user = getStoredUser()
        val token = getAccessToken()
        _sessionState.value = if (user != null && token != null) {
            SessionState.Authenticated(user)
        } else {
            SessionState.Unauthenticated
        }
    }

    suspend fun saveSession(authResponse: AuthResponse) {
        dataStore.edit { prefs ->
            prefs[accessTokenKey] = authResponse.accessToken
            prefs[refreshTokenKey] = authResponse.refreshToken
            prefs[userJsonKey] = json.encodeToString(User.serializer(), authResponse.user)
        }
        _sessionState.value = SessionState.Authenticated(authResponse.user)
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
            prefs.remove(userJsonKey)
        }
        _sessionState.value = SessionState.Unauthenticated
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[accessTokenKey] }.first()
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[refreshTokenKey] }.first()
    }

    private suspend fun getStoredUser(): User? {
        val userJson = dataStore.data.map { it[userJsonKey] }.first() ?: return null
        return try {
            json.decodeFromString(User.serializer(), userJson)
        } catch (e: Exception) {
            null
        }
    }
}
