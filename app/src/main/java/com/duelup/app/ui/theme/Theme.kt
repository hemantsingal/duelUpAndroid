package com.duelup.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class DuelUpColors(
    val success: Color = Success,
    val gold: Color = Gold,
    val timerSafe: Color = TimerSafe,
    val timerWarning: Color = TimerWarning,
    val timerDanger: Color = TimerDanger,
    val difficultyEasy: Color = DifficultyEasy,
    val difficultyMedium: Color = DifficultyMedium,
    val difficultyHard: Color = DifficultyHard,
    val surfaceLight: Color = SurfaceLight
)

val LocalDuelUpColors = staticCompositionLocalOf { DuelUpColors() }

// Use LightColorScheme for a vibrant, kid-friendly look
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnPrimary
)

@Composable
fun DuelUpTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDuelUpColors provides DuelUpColors()) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object DuelUpThemeExtras {
    val colors: DuelUpColors
        @Composable
        get() = LocalDuelUpColors.current
}
