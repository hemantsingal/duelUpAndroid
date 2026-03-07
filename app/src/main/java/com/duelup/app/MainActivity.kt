package com.duelup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duelup.app.data.remote.interceptor.NetworkMonitor
import com.duelup.app.ui.navigation.DuelUpNavHost
import com.duelup.app.ui.theme.DuelUpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DuelUpTheme {
                DuelUpNavHost(networkMonitor = networkMonitor)
            }
        }
    }
}
