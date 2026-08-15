package com.LetterQuest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.ui.screens.AboutScreen
import com.LetterQuest.ui.screens.AuthScreen
import com.LetterQuest.ui.screens.AchievementsScreen
import com.LetterQuest.ui.screens.CategoryProgressScreen
import com.LetterQuest.ui.screens.CategorySelectScreen
import com.LetterQuest.ui.screens.DailyChallengeScreen
import com.LetterQuest.ui.screens.GameHistoryScreen
import com.LetterQuest.ui.screens.GameplayScreen
import com.LetterQuest.ui.screens.GameSetupScreen
import com.LetterQuest.ui.screens.HomeScreen
import com.LetterQuest.ui.screens.LeaderboardScreen
import com.LetterQuest.ui.screens.PrivacyPolicyScreen
import com.LetterQuest.ui.screens.ProfileScreen
import com.LetterQuest.ui.screens.SettingsScreen
import com.LetterQuest.ui.screens.ShopScreen
import com.LetterQuest.ui.screens.StatisticsScreen
import com.LetterQuest.ui.screens.TermsScreen
import com.LetterQuest.ui.screens.ThemeCustomizationScreen
import com.LetterQuest.ui.screens.TutorialScreen

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
        composable(NavigationRoute.Auth.route) {
            AuthScreen(navController)
        }
    }
}
