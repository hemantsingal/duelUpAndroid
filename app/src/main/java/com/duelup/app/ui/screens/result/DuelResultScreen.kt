package com.duelup.app.ui.screens.result

import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.duelup.app.R
import com.duelup.app.ui.components.ScoreBar
import com.duelup.app.ui.navigation.Screen
import com.duelup.app.ui.theme.DuelUpThemeExtras
import com.duelup.app.util.SoundEffect
import com.duelup.app.util.SoundManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface ResultSoundEntryPoint {
    fun soundManager(): SoundManager
}

@Composable
fun DuelResultScreen(
    navController: NavController,
    viewModel: DuelResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuelUpThemeExtras.colors
    val context = LocalContext.current
    val soundManager = remember(context) {
        EntryPointAccessors.fromApplication(context, ResultSoundEntryPoint::class.java).soundManager()
    }

    // Play victory/defeat sound
    LaunchedEffect(uiState.result) {
        when (uiState.result) {
            "win" -> soundManager.play(SoundEffect.VICTORY)
            "lose" -> soundManager.play(SoundEffect.DEFEAT)
        }
    }

    // Play score count-up sound
    LaunchedEffect(uiState.playerScore) {
        if (uiState.playerScore > 0) {
            soundManager.play(SoundEffect.SCORE_UP)
        }
    }

    val bannerColor = when (uiState.result) {
        "win" -> colors.success
        "lose" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bannerText = when (uiState.result) {
        "win" -> "VICTORY!"
        "lose" -> "DEFEAT"
        else -> "DRAW"
    }

    // Animated score count-up
    var targetPlayerScore by remember { mutableIntStateOf(0) }
    var targetOpponentScore by remember { mutableIntStateOf(0) }
    val animatedPlayerScore by animateIntAsState(
        targetValue = targetPlayerScore,
        animationSpec = tween(1500),
        label = "player_score"
    )
    val animatedOpponentScore by animateIntAsState(
        targetValue = targetOpponentScore,
        animationSpec = tween(1500),
        label = "opponent_score"
    )

    LaunchedEffect(uiState.playerScore) {
        targetPlayerScore = uiState.playerScore
        targetOpponentScore = uiState.opponentScore
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Lottie animation for result
        val lottieRes = when (uiState.result) {
            "win" -> R.raw.lottie_victory
            "lose" -> R.raw.lottie_defeat
            else -> null
        }
        if (lottieRes != null) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(160.dp)
            )
        }

        // Banner
        Text(
            text = bannerText,
            style = MaterialTheme.typography.displayLarge,
            color = bannerColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Y", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("You", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(animatedPlayerScore.toString(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            }

            Text("vs", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("O", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Opponent", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(animatedOpponentScore.toString(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score bars
        Row(modifier = Modifier.fillMaxWidth()) {
            ScoreBar(score = uiState.playerScore, maxScore = maxOf(uiState.playerScore, uiState.opponentScore, 1), modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            ScoreBar(score = uiState.opponentScore, maxScore = maxOf(uiState.playerScore, uiState.opponentScore, 1), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow("Correct", "${uiState.correctAnswers}/${uiState.totalQuestions}")
            if (uiState.totalQuestions > 0) {
                StatRow("Accuracy", "${(uiState.correctAnswers * 100 / uiState.totalQuestions)}%")
            }
            if (uiState.avgTimeMs > 0) {
                StatRow("Avg Time", String.format("%.1fs", uiState.avgTimeMs / 1000.0))
            }
            if (uiState.ratingChange != 0) {
                StatRow(
                    label = "Rating",
                    value = if (uiState.ratingChange > 0) "+${uiState.ratingChange}" else "${uiState.ratingChange}",
                    valueColor = if (uiState.ratingChange > 0) colors.success else MaterialTheme.colorScheme.error
                )
            }
            if (uiState.xpEarned > 0) {
                StatRow("XP Earned", "+${uiState.xpEarned}", colors.gold)
            }
            if (uiState.coinsEarned > 0) {
                StatRow("Coins", "+${uiState.coinsEarned}", colors.gold)
            }
        }

        // Per-question breakdown
        if (uiState.questionBreakdowns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Question Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Q#", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text("You", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Opp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            uiState.questionBreakdowns.forEach { q ->
                QuestionBreakdownRow(q)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.navigate(Screen.DuelReplay.createRoute(uiState.duelId)) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Replay")
            }

            Button(
                onClick = {
                    if (uiState.quizId.isNotEmpty()) {
                        navController.navigate(Screen.Matchmaking.createRoute(uiState.quizId))
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play Again")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to Home")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuestionBreakdownRow(q: QuestionBreakdown) {
    val colors = DuelUpThemeExtras.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Question number
        Text(
            text = "${q.index + 1}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        // Player result
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (q.playerCorrect) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                tint = if (q.playerCorrect) colors.success else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1fs", q.playerTimeMs / 1000.0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (q.playerPoints > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${q.playerPoints}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.success,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Opponent result
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (q.opponentCorrect) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                tint = if (q.opponentCorrect) colors.success else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1fs", q.opponentTimeMs / 1000.0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (q.opponentPoints > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${q.opponentPoints}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.success,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}
