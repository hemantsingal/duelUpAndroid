package com.duelup.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.duelup.app.data.remote.interceptor.NetworkMonitor
import com.duelup.app.ui.components.OfflineBanner

@Composable
fun DuelUpNavHost(networkMonitor: NetworkMonitor) {
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            OfflineBanner(isOffline = !isOnline)
            NavGraph(
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
