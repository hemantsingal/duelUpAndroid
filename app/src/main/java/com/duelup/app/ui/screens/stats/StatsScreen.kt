package com.duelup.app.ui.screens.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.duelup.app.ui.components.ErrorScreen
import com.duelup.app.ui.components.FullScreenLoading
import com.duelup.app.ui.theme.DuelUpThemeExtras
import com.duelup.app.util.formatResponseTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Stats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(paddingValues))
            uiState.error != null -> ErrorScreen(
                message = uiState.error!!,
                onRetry = viewModel::loadStats,
                modifier = Modifier.padding(paddingValues)
            )
            uiState.stats != null -> {
                val stats = uiState.stats!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Win/Loss/Draw donut chart
                    val successColor = DuelUpThemeExtras.colors.success
                    val errorColor = MaterialTheme.colorScheme.error
                    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

                    WinLossDonut(
                        wins = stats.duelsWon,
                        losses = stats.duelsLost,
                        draws = stats.duelsDraw,
                        winColor = successColor,
                        lossColor = errorColor,
                        drawColor = surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LegendDot(color = successColor, label = "Wins (${stats.duelsWon})")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendDot(color = errorColor, label = "Losses (${stats.duelsLost})")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendDot(color = surfaceVariant, label = "Draws (${stats.duelsDraw})")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stat cards grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Total Duels",
                            value = stats.totalDuels.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Accuracy",
                            value = "${(stats.accuracy * 100).toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Total XP",
                            value = stats.totalXp.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Avg Response",
                            value = stats.avgResponseTimeMs.formatResponseTime(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Current Streak",
                            value = stats.currentStreak.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Best Streak",
                            value = stats.bestStreak.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Questions Answered",
                            value = stats.totalQuestionsAnswered.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Correct Answers",
                            value = stats.correctAnswers.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WinLossDonut(
    wins: Int,
    losses: Int,
    draws: Int,
    winColor: Color,
    lossColor: Color,
    drawColor: Color
) {
    val total = wins + losses + draws
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedSweep by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_anim"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 28.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)

            if (total == 0) {
                drawArc(
                    color = drawColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
            } else {
                val winAngle = (wins.toFloat() / total) * 360f * animatedSweep
                val lossAngle = (losses.toFloat() / total) * 360f * animatedSweep
                val drawAngle = (draws.toFloat() / total) * 360f * animatedSweep

                drawArc(
                    color = winColor,
                    startAngle = -90f,
                    sweepAngle = winAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
                drawArc(
                    color = lossColor,
                    startAngle = -90f + winAngle,
                    sweepAngle = lossAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
                drawArc(
                    color = drawColor,
                    startAngle = -90f + winAngle + lossAngle,
                    sweepAngle = drawAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = total.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
