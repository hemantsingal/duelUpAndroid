package com.duelup.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duelup.app.ui.theme.DuelUpThemeExtras

enum class OptionState {
    DEFAULT,
    SELECTED,
    CORRECT,
    WRONG,
    DISABLED
}

@Composable
fun OptionButton(
    index: Int,
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DuelUpThemeExtras.colors
    val letter = ('A' + index).toString()

    val containerColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.DEFAULT -> MaterialTheme.colorScheme.surface
            OptionState.SELECTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            OptionState.CORRECT -> colors.success.copy(alpha = 0.2f)
            OptionState.WRONG -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            OptionState.DISABLED -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "option_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
            OptionState.SELECTED -> MaterialTheme.colorScheme.primary
            OptionState.CORRECT -> colors.success
            OptionState.WRONG -> MaterialTheme.colorScheme.error
            OptionState.DISABLED -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "option_border"
    )

    val textColor = when (state) {
        OptionState.DEFAULT -> MaterialTheme.colorScheme.onSurface
        OptionState.SELECTED -> MaterialTheme.colorScheme.primary
        OptionState.CORRECT -> colors.success
        OptionState.WRONG -> MaterialTheme.colorScheme.error
        OptionState.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        enabled = state == OptionState.DEFAULT,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$letter)",
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}
