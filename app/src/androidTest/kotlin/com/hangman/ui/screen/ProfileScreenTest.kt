package com.hangman.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.hangman.ui.screens.ProfileScreen
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileScreenRendersTitle() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ProfileScreen(navController)
        }

        composeTestRule.onNodeWithText("Profile").assertExists()
    }

    @Test
    fun testProfileScreenRendersStatistics() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ProfileScreen(navController)
        }

        composeTestRule.onNodeWithText("Player Statistics").assertExists()
    }

    @Test
    fun testProfileScreenRendersAchievementsSection() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ProfileScreen(navController)
        }

        composeTestRule.onNodeWithText("Achievements").assertExists()
    }
}
