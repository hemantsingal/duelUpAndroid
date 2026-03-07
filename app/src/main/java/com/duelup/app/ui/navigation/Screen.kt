package com.duelup.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object GuestLogin : Screen("guest_login")
    data object Home : Screen("home")
    data object QuizList : Screen("quiz_list?category={category}") {
        fun createRoute(category: String? = null): String {
            return if (category != null) "quiz_list?category=$category" else "quiz_list"
        }
    }
    data object QuizDetail : Screen("quiz_detail/{quizId}") {
        fun createRoute(quizId: String) = "quiz_detail/$quizId"
    }
    data object Matchmaking : Screen("matchmaking/{quizId}") {
        fun createRoute(quizId: String) = "matchmaking/$quizId"
    }
    data object Duel : Screen("duel/{duelId}") {
        fun createRoute(duelId: String) = "duel/$duelId"
    }
    data object DuelResult : Screen("duel_result/{duelId}") {
        fun createRoute(duelId: String) = "duel_result/$duelId"
    }
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Stats : Screen("stats")
    data object DuelHistory : Screen("duel_history")
    data object DuelReplay : Screen("duel_replay/{duelId}") {
        fun createRoute(duelId: String) = "duel_replay/$duelId"
    }
    data object Leaderboard : Screen("leaderboard")
    data object Settings : Screen("settings")
    data object Achievements : Screen("achievements")
    data object Challenges : Screen("challenges")
    data object Friends : Screen("friends")
    data object DirectChallenge : Screen("direct_challenge/{friendId}") {
        fun createRoute(friendId: String) = "direct_challenge/$friendId"
    }
}
