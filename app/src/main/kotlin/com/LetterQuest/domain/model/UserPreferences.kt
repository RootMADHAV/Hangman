package com.LetterQuest.domain.model

data class UserPreferences(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val darkTheme: Boolean = false,
    val defaultDifficulty: Difficulty = Difficulty.MEDIUM,
    val notificationsEnabled: Boolean = true,
    val themePresetId: String? = null,
    /** Whether the in-game power-up / hints panel is shown during gameplay. */
    val hintsEnabled: Boolean = true,
    val adsRemoved: Boolean = false
)
