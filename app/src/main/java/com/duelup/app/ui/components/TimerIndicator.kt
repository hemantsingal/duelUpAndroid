package com.duelup.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.duelup.app.ui.theme.DuelUpThemeExtras

@Composable
fun TimerIndicator(
    seconds: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = DuelUpThemeExtras.colors
    val timerColor by animateColorAsState(
        targetValue = when {
            seconds <= 3 -> colors.timerDanger
            seconds <= 5 -> colors.timerWarning
            else -> colors.timerSafe
        },
        animationSpec = tween(300),
        label = "timer_color"
    )

    // Pulse when < 3s
    val pulse = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (seconds <= 3) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse_scale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.matchParentSize(),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 4.dp
        )
        Text(
            text = seconds.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = timerColor
        )
    }
}
