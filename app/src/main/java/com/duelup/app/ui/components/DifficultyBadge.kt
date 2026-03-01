package com.duelup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.duelup.app.ui.theme.DuelUpThemeExtras

@Composable
fun DifficultyBadge(
    difficulty: String,
    modifier: Modifier = Modifier
) {
    val colors = DuelUpThemeExtras.colors
    val (bgColor, label) = when (difficulty.uppercase()) {
        "EASY" -> colors.difficultyEasy to "Easy"
        "MEDIUM" -> colors.difficultyMedium to "Medium"
        "HARD" -> colors.difficultyHard to "Hard"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to difficulty
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = bgColor,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bgColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
