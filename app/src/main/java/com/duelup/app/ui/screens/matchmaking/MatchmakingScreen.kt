package com.duelup.app.ui.screens.matchmaking

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.duelup.app.R
import com.duelup.app.ui.components.RatingBadge
import com.duelup.app.ui.navigation.Screen
import com.duelup.app.util.SoundEffect
import com.duelup.app.util.SoundManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface MatchmakingSoundEntryPoint {
    fun soundManager(): SoundManager
}

@Composable
fun MatchmakingScreen(
    navController: NavController,
    viewModel: MatchmakingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val soundManager = remember(context) {
        EntryPointAccessors.fromApplication(context, MatchmakingSoundEntryPoint::class.java).soundManager()
    }

    // Play match found sound + countdown sounds
    LaunchedEffect(state) {
        val s = state
        if (s is MatchmakingState.Found) {
            if (s.countdown == 3) {
                soundManager.play(SoundEffect.MATCH_FOUND)
            }
            if (s.countdown in 1..3) {
                soundManager.play(SoundEffect.COUNTDOWN)
            }
        }
    }

    // Navigate on found + countdown done
    LaunchedEffect(state) {
        val s = state
        if (s is MatchmakingState.Found && s.countdown <= 0) {
            navController.navigate(Screen.Duel.createRoute(s.duelId)) {
                popUpTo(Screen.Matchmaking.route) { inclusive = true }
            }
        }
        if (s is MatchmakingState.Cancelled) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player vs Opponent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerCard(
                    username = viewModel.currentUser?.username ?: "You",
                    rating = viewModel.currentUser?.rating ?: 1000,
                    isRevealed = true
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.secondary
                )

                when (val s = state) {
                    is MatchmakingState.Found -> PlayerCard(
                        username = s.opponent.username,
                        rating = s.opponent.rating,
                        isRevealed = true
                    )
                    else -> PlayerCard(
                        username = "???",
                        rating = 0,
                        isRevealed = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status area
            when (val s = state) {
                is MatchmakingState.Connecting -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connecting...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is MatchmakingState.Searching -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_matchmaking_search))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Searching for opponent...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${s.waitTimeSeconds}s",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is MatchmakingState.Found -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_matchmaking_found))
                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier.size(120.dp)
                    )
                    Text(
                        text = "Match Found!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (s.countdown > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Starting in ${s.countdown}...",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MatchmakingState.Error -> {
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
                is MatchmakingState.Cancelled -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state is MatchmakingState.Searching || state is MatchmakingState.Connecting) {
                OutlinedButton(
                    onClick = { viewModel.cancelSearch() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(
    username: String,
    rating: Int,
    isRevealed: Boolean
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (!isRevealed) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(if (!isRevealed) scale else 1f)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (isRevealed) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRevealed) username.take(1).uppercase() else "?",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isRevealed) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isRevealed && rating > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            RatingBadge(rating = rating)
        }
    }
}
