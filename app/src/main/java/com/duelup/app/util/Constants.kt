package com.duelup.app.util

object Constants {
    const val BASE_URL = "http://10.0.2.2:3000/api/v1/"
    const val SOCKET_URL = "http://10.0.2.2:3000"
    const val SOCKET_NAMESPACE = "/duels"

    const val DATASTORE_NAME = "duelup_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_JSON = "user_json"

    const val DEFAULT_PAGE_SIZE = 20
    const val SEARCH_DEBOUNCE_SHORT_MS = 400L  // 1-2 chars: wait longer
    const val SEARCH_DEBOUNCE_LONG_MS = 250L   // 3+ chars: respond faster
    const val MATCHMAKING_TIMEOUT_MS = 30_000L
    const val RECONNECT_GRACE_PERIOD_MS = 15_000L
    const val QUESTION_RESULT_DELAY_MS = 3_000L
}
