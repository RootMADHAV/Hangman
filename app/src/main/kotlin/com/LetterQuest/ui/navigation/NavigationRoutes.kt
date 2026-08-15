package com.LetterQuest.ui.navigation

sealed class NavigationRoute(val route: String) {
    data object Home : NavigationRoute("home")
    data object GameSetup : NavigationRoute("game_setup")

    class CategorySelect(difficulty: String) : NavigationRoute("category_select/$difficulty") {
        companion object {
            const val ROUTE_BASE = "category_select"
            fun routeWithDifficulty(difficulty: String) = "$ROUTE_BASE/$difficulty"
        }
    }

    class Gameplay(difficulty: String) : NavigationRoute("gameplay/$difficulty") {
        companion object {
            const val ROUTE_BASE = "gameplay"
            fun routeWithDifficulty(difficulty: String) = "$ROUTE_BASE/$difficulty"
            fun routeWithCategory(difficulty: String, categoryId: String) =
                "$ROUTE_BASE/$difficulty?category=$categoryId"
        }
    }

    data object Statistics : NavigationRoute("statistics")
    data object Achievements : NavigationRoute("achievements")
    data object Settings : NavigationRoute("settings")
    data object About : NavigationRoute("about")
    data object GameHistory : NavigationRoute("game_history")
    data object Leaderboard : NavigationRoute("leaderboard")
    data object Profile : NavigationRoute("profile")
    data object ThemeCustomization : NavigationRoute("theme_customization")
    data object DailyChallenge : NavigationRoute("daily_challenge")

    /** The daily puzzle itself; the word is fixed by the date, so it takes no arguments. */
    data object DailyGameplay : NavigationRoute("daily_gameplay")

    data object Shop : NavigationRoute("shop")
    data object CategoryProgress : NavigationRoute("category_progress")
    data object Tutorial : NavigationRoute("tutorial")
    data object PrivacyPolicy : NavigationRoute("privacy_policy")
    data object Terms : NavigationRoute("terms")
    data object Auth : NavigationRoute("auth")
}
