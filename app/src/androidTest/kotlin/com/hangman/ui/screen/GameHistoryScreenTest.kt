package com.hangman.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameHistoryEntry
import com.hangman.ui.screens.GameHistoryScreen
import org.junit.Rule
import org.junit.Test

class GameHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGameHistoryScreenRendersTitle() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            GameHistoryScreen(navController)
        }

        composeTestRule.onNodeWithText("Game History").assertExists()
    }

    @Test
    fun testGameHistoryEmptyState() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            GameHistoryScreen(navController)
        }

        composeTestRule.onNodeWithText("Game History").assertExists()
    }
}
