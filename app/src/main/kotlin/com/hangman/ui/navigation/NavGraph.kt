package com.hangman.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hangman.domain.model.WordCategory
import com.hangman.ui.screens.AboutScreen
import com.hangman.ui.screens.AchievementsScreen
import com.hangman.ui.screens.CategoryProgressScreen
import com.hangman.ui.screens.CategorySelectScreen
import com.hangman.ui.screens.DailyChallengeScreen
import com.hangman.ui.screens.GameHistoryScreen
import com.hangman.ui.screens.GameplayScreen
import com.hangman.ui.screens.GameSetupScreen
import com.hangman.ui.screens.HomeScreen
import com.hangman.ui.screens.LeaderboardScreen
import com.hangman.ui.screens.PrivacyPolicyScreen
import com.hangman.ui.screens.ProfileScreen
import com.hangman.ui.screens.SettingsScreen
import com.hangman.ui.screens.ShopScreen
import com.hangman.ui.screens.StatisticsScreen
import com.hangman.ui.screens.TermsScreen
import com.hangman.ui.screens.ThemeCustomizationScreen
import com.hangman.ui.screens.TutorialScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = NavigationRoute.Home.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoute.Home.route) {
            HomeScreen(navController)
        }
        composable(NavigationRoute.GameSetup.route) {
            GameSetupScreen(navController)
        }
        composable("${NavigationRoute.CategorySelect.ROUTE_BASE}/{difficulty}") {
            val difficulty = it.arguments?.getString("difficulty") ?: "MEDIUM"
            CategorySelectScreen(navController, difficulty)
        }
        composable(
            route = "${NavigationRoute.Gameplay.ROUTE_BASE}/{difficulty}?category={category}",
            arguments = listOf(
                navArgument("difficulty") {
                    type = NavType.StringType
                    defaultValue = "MEDIUM"
                },
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = WordCategory.ALL_CATEGORIES_ID
                }
            )
        ) {
            val difficulty = it.arguments?.getString("difficulty") ?: "MEDIUM"
            val category = it.arguments?.getString("category") ?: WordCategory.ALL_CATEGORIES_ID
            GameplayScreen(navController, difficulty, category)
        }
        composable(NavigationRoute.Statistics.route) {
            StatisticsScreen(navController)
        }
        composable(NavigationRoute.Achievements.route) {
            AchievementsScreen(navController)
        }
        composable(NavigationRoute.Settings.route) {
            SettingsScreen(navController)
        }
        composable(NavigationRoute.About.route) {
            AboutScreen(navController)
        }
        composable(NavigationRoute.GameHistory.route) {
            GameHistoryScreen(navController)
        }
        composable(NavigationRoute.Leaderboard.route) {
            LeaderboardScreen(navController)
        }
        composable(NavigationRoute.Profile.route) {
            ProfileScreen(navController)
        }
        composable(NavigationRoute.ThemeCustomization.route) {
            ThemeCustomizationScreen(navController)
        }
        composable(NavigationRoute.DailyChallenge.route) {
            DailyChallengeScreen(navController)
        }
        composable(NavigationRoute.DailyGameplay.route) {
            GameplayScreen(navController, isDailyChallenge = true)
        }
        composable(NavigationRoute.Shop.route) {
            ShopScreen(navController)
        }
        composable(NavigationRoute.CategoryProgress.route) {
            CategoryProgressScreen(navController)
        }
        composable(NavigationRoute.Tutorial.route) {
            TutorialScreen(navController)
        }
        composable(NavigationRoute.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController)
        }
        composable(NavigationRoute.Terms.route) {
            TermsScreen(navController)
        }
    }
}
