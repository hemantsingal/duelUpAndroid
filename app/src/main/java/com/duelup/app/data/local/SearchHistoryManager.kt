package com.duelup.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val recentSearchesKey = stringPreferencesKey("recent_searches")

    val recentSearches: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[recentSearchesKey]
            ?.split(DELIMITER)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        dataStore.edit { prefs ->
            val existing = prefs[recentSearchesKey]
                ?.split(DELIMITER)
                ?.filter { it.isNotBlank() }
                ?.toMutableList()
                ?: mutableListOf()
            existing.remove(trimmed)
            existing.add(0, trimmed)
            prefs[recentSearchesKey] = existing.take(MAX_RECENT).joinToString(DELIMITER)
        }
    }

    suspend fun removeSearch(query: String) {
        dataStore.edit { prefs ->
            val existing = prefs[recentSearchesKey]
                ?.split(DELIMITER)
                ?.filter { it.isNotBlank() && it != query.trim() }
                ?: emptyList()
            prefs[recentSearchesKey] = existing.joinToString(DELIMITER)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(recentSearchesKey)
        }
    }

    private companion object {
        const val DELIMITER = "|||"
        const val MAX_RECENT = 10
    }
}
