package com.duelup.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        label = "Home",
        icon = Icons.Rounded.Home
    )

    data object Leaderboard : BottomNavItem(
        route = Screen.Leaderboard.route,
        label = "Leaderboard",
        icon = Icons.Rounded.EmojiEvents
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        label = "Profile",
        icon = Icons.Rounded.Person
    )

    companion object {
        val items = listOf(Home, Leaderboard, Profile)
    }
}
