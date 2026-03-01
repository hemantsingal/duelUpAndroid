package com.duelup.app.ui.screens.duel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.duelup.app.ui.components.OptionButton
import com.duelup.app.ui.components.OptionState
import com.duelup.app.ui.components.ScoreBar
import com.duelup.app.ui.components.StreakIndicator
import com.duelup.app.ui.components.TimerIndicator
import com.duelup.app.ui.navigation.Screen
import com.duelup.app.ui.theme.DuelUpThemeExtras
import com.duelup.app.util.HapticFeedback

@Composable
fun DuelScreen(
    navController: NavController,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val colors = DuelUpThemeExtras.colors

    // Navigate to result when duel ends
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == DuelPhase.ENDED) {
            kotlinx.coroutines.delay(1500)
            navController.navigate(Screen.DuelResult.createRoute(uiState.duelId)) {
                popUpTo(Screen.Duel.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.phase == DuelPhase.WAITING) {
            // Waiting for duel to start
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for duel to start...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (uiState.currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header: scores and question counter
                DuelHeader(
                    playerScore = uiState.playerScore,
                    opponentScore = uiState.opponentScore,
                    questionIndex = (uiState.currentQuestion?.index ?: 0) + 1,
                    totalQuestions = uiState.totalQuestions,
                    maxScore = maxOf(uiState.playerScore, uiState.opponentScore, 100)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimerIndicator(
                        seconds = uiState.timerSeconds,
                        progress = uiState.timerProgress
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Streak indicator
                StreakIndicator(streak = uiState.streak)

                // Question area with animated transitions
                AnimatedContent(
                    targetState = uiState.currentQuestion,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    },
                    label = "question_transition"
                ) { question ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Question text
                        Text(
                            text = question?.text ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Options
                        question?.options?.forEachIndexed { index, optionText ->
                            val optionState = getOptionState(
                                index = index,
                                selectedAnswer = uiState.selectedAnswer,
                                correctAnswer = uiState.correctAnswer,
                                isRevealing = uiState.phase == DuelPhase.REVEALING,
                                isLocked = uiState.isAnswerLocked
                            )

                            OptionButton(
                                index = index,
                                text = optionText,
                                state = optionState,
                                onClick = {
                                    HapticFeedback.lightTap(view)
                                    viewModel.selectAnswer(index)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Points earned overlay
                AnimatedVisibility(
                    visible = uiState.phase == DuelPhase.REVEALING && uiState.pointsEarned > 0
                ) {
                    Text(
                        text = "+${uiState.pointsEarned} pts",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.success,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DuelHeader(
    playerScore: Int,
    opponentScore: Int,
    questionIndex: Int,
    totalQuestions: Int,
    maxScore: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player score
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "You",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = playerScore.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            ScoreBar(score = playerScore, maxScore = maxScore)
        }

        // Question counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Q $questionIndex/$totalQuestions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Opponent score
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Opponent",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = opponentScore.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            ScoreBar(score = opponentScore, maxScore = maxScore)
        }
    }
}

private fun getOptionState(
    index: Int,
    selectedAnswer: Int?,
    correctAnswer: Int?,
    isRevealing: Boolean,
    isLocked: Boolean
): OptionState {
    if (isRevealing && correctAnswer != null) {
        return when {
            index == correctAnswer -> OptionState.CORRECT
            index == selectedAnswer && selectedAnswer != correctAnswer -> OptionState.WRONG
            else -> OptionState.DISABLED
        }
    }
    if (isLocked) {
        return if (index == selectedAnswer) OptionState.SELECTED else OptionState.DISABLED
    }
    return OptionState.DEFAULT
}
