package com.hangman.domain.repository

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.TutorialSettings
import com.hangman.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun getPreferences(): Result<UserPreferences>
    suspend fun setSoundEnabled(enabled: Boolean): Result<Unit>
    suspend fun setMusicEnabled(enabled: Boolean): Result<Unit>
    suspend fun setDarkTheme(enabled: Boolean): Result<Unit>
    suspend fun setDefaultDifficulty(difficulty: Difficulty): Result<Unit>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
    suspend fun setThemePreset(presetId: String): Result<Unit>
    suspend fun setHintsEnabled(enabled: Boolean): Result<Unit>
    fun observeUnlockedThemes(): Flow<Set<String>>
    suspend fun unlockTheme(themeId: String): Result<Unit>
    suspend fun getTutorialSettings(): Result<TutorialSettings>
    suspend fun setTutorialSetting(key: String, enabled: Boolean): Result<Unit>
    fun observeTutorialSettings(): Flow<TutorialSettings>
    suspend fun setTutorialSeen(type: String): Result<Unit>
    fun observeLaunchCount(): Flow<Int>
    suspend fun getLaunchCount(): Result<Int>
    suspend fun incrementLaunchCount(): Result<Int>
}
