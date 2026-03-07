package com.duelup.app.ui.screens.duel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.duelup.app.R
import com.duelup.app.ui.components.OptionButton
import com.duelup.app.ui.components.OptionState

import com.duelup.app.ui.components.TimerIndicator
import com.duelup.app.ui.navigation.Screen
import com.duelup.app.ui.theme.DuelUpThemeExtras
import com.duelup.app.util.HapticFeedback
import com.duelup.app.util.SoundEffect
import com.duelup.app.util.SoundManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface DuelSoundEntryPoint {
    fun soundManager(): SoundManager
}

@Composable
fun DuelScreen(
    navController: NavController,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val colors = DuelUpThemeExtras.colors
    val context = LocalContext.current
    val soundManager = remember(context) {
        EntryPointAccessors.fromApplication(context, DuelSoundEntryPoint::class.java).soundManager()
    }

    // Animated scores
    val animatedPlayerScore by animateIntAsState(
        targetValue = uiState.playerScore,
        animationSpec = tween(600),
        label = "player_score"
    )
    val animatedOpponentScore by animateIntAsState(
        targetValue = uiState.opponentScore,
        animationSpec = tween(600),
        label = "opponent_score"
    )

    // Sound effects for game events
    LaunchedEffect(uiState.phase) {
        when (uiState.phase) {
            DuelPhase.PLAYING -> soundManager.play(SoundEffect.QUESTION_IN)
            DuelPhase.REVEALING -> {
                val isCorrect = uiState.questionResult?.player?.isCorrect == true
                if (isCorrect) {
                    soundManager.play(SoundEffect.CORRECT)
                } else {
                    soundManager.play(SoundEffect.WRONG)
                }
            }
            else -> {}
        }
    }

    // Timer urgent sound when time is low
    LaunchedEffect(uiState.timerSeconds) {
        if (uiState.phase == DuelPhase.PLAYING && uiState.timerSeconds in 1..3) {
            soundManager.play(SoundEffect.TIMER_URGENT)
        }
    }

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Waiting for duel to start...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header: player | timer | opponent
                DuelHeader(
                    playerName = uiState.playerName,
                    opponentName = uiState.opponentName,
                    playerScore = animatedPlayerScore,
                    opponentScore = animatedOpponentScore,
                    questionIndex = (uiState.currentQuestion?.index ?: 0) + 1,
                    totalQuestions = uiState.totalQuestions,
                    timerSeconds = uiState.timerSeconds,
                    timerProgress = uiState.timerProgress,
                    dots = uiState.questionDots,
                    currentDotIndex = uiState.currentQuestion?.index ?: 0
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        // Quiz title
                        if (uiState.quizTitle.isNotEmpty()) {
                            Text(
                                text = uiState.quizTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = question?.text ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        question?.options?.forEachIndexed { index, optionText ->
                            val optionState = getOptionState(
                                index = index,
                                selectedAnswer = uiState.selectedAnswer,
                                correctAnswer = uiState.correctAnswer,
                                isRevealing = uiState.phase == DuelPhase.REVEALING,
                                isLocked = uiState.isAnswerLocked,
                                playerIsCorrect = uiState.questionResult?.player?.isCorrect == true
                            )

                            OptionButton(
                                index = index,
                                text = optionText,
                                state = optionState,
                                onClick = {
                                    HapticFeedback.lightTap(view)
                                    soundManager.play(SoundEffect.TAP)
                                    viewModel.selectAnswer(index)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Points earned overlay + Lottie feedback
                AnimatedVisibility(
                    visible = uiState.phase == DuelPhase.REVEALING
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.questionResult?.player?.isCorrect == true) {
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_correct_answer))
                            LottieAnimation(composition = composition, modifier = Modifier.size(80.dp))
                        } else if (uiState.selectedAnswer != null) {
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_wrong_answer))
                            LottieAnimation(composition = composition, modifier = Modifier.size(80.dp))
                        }

                        if (uiState.pointsEarned > 0) {
                            Text(
                                text = "+${uiState.pointsEarned} pts",
                                style = MaterialTheme.typography.headlineLarge,
                                color = colors.success,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun DuelHeader(
    playerName: String,
    opponentName: String,
    playerScore: Int,
    opponentScore: Int,
    questionIndex: Int,
    totalQuestions: Int,
    timerSeconds: Int,
    timerProgress: Float,
    dots: List<QuestionDotState>,
    currentDotIndex: Int
) {
    val colors = DuelUpThemeExtras.colors
    val borderColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player column: name, score, dots
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playerName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                playerScore.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (dots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    dots.forEach { dot ->
                        val isCurrent = dot.index == currentDotIndex
                        val bgColor = when (dot.playerCorrect) {
                            true -> colors.success
                            false -> MaterialTheme.colorScheme.error
                            null -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(
                                    if (isCurrent) Modifier.border(1.5.dp, borderColor, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }

        // Center: timer with question counter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                "$questionIndex of $totalQuestions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            TimerIndicator(
                seconds = timerSeconds,
                progress = timerProgress,
                modifier = Modifier.size(56.dp)
            )
        }

        // Opponent column: name, score, dots
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                opponentName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                opponentScore.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            if (dots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    dots.forEach { dot ->
                        val isCurrent = dot.index == currentDotIndex
                        val bgColor = when (dot.opponentCorrect) {
                            true -> colors.success
                            false -> MaterialTheme.colorScheme.error
                            null -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(
                                    if (isCurrent) Modifier.border(1.5.dp, borderColor, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun getOptionState(
    index: Int,
    selectedAnswer: Int?,
    correctAnswer: Int?,
    isRevealing: Boolean,
    isLocked: Boolean,
    playerIsCorrect: Boolean = false
): OptionState {
    if (isRevealing && correctAnswer != null) {
        return when {
            index == selectedAnswer && playerIsCorrect -> OptionState.CORRECT
            index == correctAnswer -> OptionState.CORRECT
            index == selectedAnswer -> OptionState.WRONG
            else -> OptionState.DISABLED
        }
    }
    if (isLocked) {
        return if (index == selectedAnswer) OptionState.SELECTED else OptionState.DISABLED
    }
    return OptionState.DEFAULT
}
