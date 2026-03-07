package com.duelup.app.util

import android.os.Build

object Constants {
    // Emulator uses 10.0.2.2 to reach host machine
    // Physical device uses localhost via `adb reverse tcp:3000 tcp:3000`
    private val IS_EMULATOR = Build.FINGERPRINT.contains("generic")
            || Build.PRODUCT.contains("sdk")
            || Build.HARDWARE.contains("ranchu")

    private val HOST = if (IS_EMULATOR) "10.0.2.2" else "localhost"

    val BASE_URL = "http://$HOST:3000/api/v1/"
    val SOCKET_URL = "http://$HOST:3000"
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
