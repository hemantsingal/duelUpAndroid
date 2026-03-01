package com.duelup.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
fun StreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = streak >= 2,
        enter = scaleIn() + fadeIn(),
        modifier = modifier
    ) {
        val transition = rememberInfiniteTransition(label = "streak")
        val scale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "streak_scale"
        )

        val colors = DuelUpThemeExtras.colors

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .scale(scale)
                .padding(4.dp)
        ) {
            Text(
                text = "\uD83D\uDD25",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = " $streak streak!",
                style = MaterialTheme.typography.labelLarge,
                color = colors.gold
            )
        }
    }
}
