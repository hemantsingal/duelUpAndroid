package com.duelup.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.duelup.app.ui.screens.auth.GuestLoginScreen
import com.duelup.app.ui.screens.duel.DuelScreen
import com.duelup.app.ui.screens.history.DuelHistoryScreen
import com.duelup.app.ui.screens.home.HomeScreen
import com.duelup.app.ui.screens.leaderboard.LeaderboardScreen
import com.duelup.app.ui.screens.matchmaking.MatchmakingScreen
import com.duelup.app.ui.screens.profile.EditProfileScreen
import com.duelup.app.ui.screens.profile.ProfileScreen
import com.duelup.app.ui.screens.quiz.QuizDetailScreen
import com.duelup.app.ui.screens.quiz.QuizListScreen
import com.duelup.app.ui.screens.replay.DuelReplayScreen
import com.duelup.app.ui.screens.result.DuelResultScreen
import com.duelup.app.ui.screens.splash.SplashScreen
import com.duelup.app.ui.screens.stats.StatsScreen

private const val ANIM_DURATION = 300

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.GuestLogin.route) {
            GuestLoginScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = "quiz_list?category={category}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            QuizListScreen(navController = navController)
        }

        composable(
            route = Screen.QuizDetail.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            )
        ) {
            QuizDetailScreen(navController = navController)
        }

        composable(
            route = Screen.Matchmaking.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            )
        ) {
            MatchmakingScreen(navController = navController)
        }

        composable(
            route = Screen.Duel.route,
            arguments = listOf(
                navArgument("duelId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "duelup://duel/{duelId}" }
            )
        ) {
            DuelScreen(navController = navController)
        }

        composable(
            route = Screen.DuelResult.route,
            arguments = listOf(
                navArgument("duelId") { type = NavType.StringType }
            )
        ) {
            DuelResultScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.Stats.route) {
            StatsScreen(navController = navController)
        }

        composable(Screen.DuelHistory.route) {
            DuelHistoryScreen(navController = navController)
        }

        composable(
            route = Screen.DuelReplay.route,
            arguments = listOf(
                navArgument("duelId") { type = NavType.StringType }
            )
        ) {
            DuelReplayScreen(navController = navController)
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController = navController)
        }
    }
}
