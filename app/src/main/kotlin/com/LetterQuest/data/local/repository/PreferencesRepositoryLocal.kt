package com.LetterQuest.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.TutorialSettings
import com.LetterQuest.domain.model.UserPreferences
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.ui.theme.ColorPresets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryLocal @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val THEME_PRESET_ID = stringPreferencesKey("theme_preset_id")
        private val HINTS_ENABLED = booleanPreferencesKey("hints_enabled")
        private val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
        private val ADS_REMOVED = booleanPreferencesKey("ads_removed")
        private val TUTORIAL_GAMEPLAY = booleanPreferencesKey("tutorial_gameplay")
        private val TUTORIAL_HINTS = booleanPreferencesKey("tutorial_hints")
        private val TUTORIAL_THEMES = booleanPreferencesKey("tutorial_themes")
        private val TUTORIAL_COMPLETED_COUNT = intPreferencesKey("tutorial_completed_count")
        private val LAUNCH_COUNT = intPreferencesKey("launch_count")
    }

    private fun Preferences.toTutorialSettings(): TutorialSettings = TutorialSettings(
        showGameplayTutorial = this[TUTORIAL_GAMEPLAY] ?: true,
        showHintsTutorial = this[TUTORIAL_HINTS] ?: true,
        showThemesTutorial = this[TUTORIAL_THEMES] ?: true,
        tutorialCompletedCount = this[TUTORIAL_COMPLETED_COUNT] ?: 0
    )

    override fun observePreferences(): Flow<UserPreferences> {
        return dataStore.data.map { mapToUserPreferences(it) }
    }

    override suspend fun getPreferences(): Result<UserPreferences> {
        return try {
            dataStore.data.map { mapToUserPreferences(it) }.first().let { Result.success(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            soundEnabled = preferences[SOUND_ENABLED] ?: true,
            musicEnabled = preferences[MUSIC_ENABLED] ?: true,
            darkTheme = preferences[DARK_THEME] ?: false,
            defaultDifficulty = try {
                Difficulty.valueOf(preferences[DEFAULT_DIFFICULTY] ?: Difficulty.MEDIUM.name)
            } catch (e: Exception) {
                Difficulty.MEDIUM
            },
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            themePresetId = preferences[THEME_PRESET_ID],
            hintsEnabled = preferences[HINTS_ENABLED] ?: true,
            adsRemoved = preferences[ADS_REMOVED] ?: false
        )
    }

    override suspend fun setMusicEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences -> preferences[MUSIC_ENABLED] = enabled }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[SOUND_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDarkTheme(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[DARK_THEME] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDefaultDifficulty(difficulty: Difficulty): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[DEFAULT_DIFFICULTY] = difficulty.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[NOTIFICATIONS_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setThemePreset(presetId: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[THEME_PRESET_ID] = presetId
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setHintsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[HINTS_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAdsRemoved(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[ADS_REMOVED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUnlockedThemes(): Flow<Set<String>> =
        dataStore.data.map { prefs ->
            ColorPresets.FREE_THEME_IDS + (prefs[UNLOCKED_THEMES] ?: emptySet())
        }

    override suspend fun unlockTheme(themeId: String): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                prefs[UNLOCKED_THEMES] = (prefs[UNLOCKED_THEMES] ?: emptySet()) + themeId
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTutorialSettings(): Result<TutorialSettings> {
        return try {
            dataStore.data.map { it.toTutorialSettings() }.first().let { Result.success(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTutorialSettings(): Flow<TutorialSettings> {
        return dataStore.data.map { it.toTutorialSettings() }
    }

    override suspend fun setTutorialSeen(type: String): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                when (type) {
                    "gameplay" -> prefs[TUTORIAL_GAMEPLAY] = false
                    "hints" -> prefs[TUTORIAL_HINTS] = false
                    "themes" -> prefs[TUTORIAL_THEMES] = false
                    else -> prefs[TUTORIAL_GAMEPLAY] = false
                }
                prefs[TUTORIAL_COMPLETED_COUNT] = (prefs[TUTORIAL_COMPLETED_COUNT] ?: 0) + 1
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLaunchCount(): Flow<Int> {
        return dataStore.data.map { it[LAUNCH_COUNT] ?: 0 }
    }

    override suspend fun getLaunchCount(): Result<Int> {
        return try {
            val count = dataStore.data.first()[LAUNCH_COUNT] ?: 0
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementLaunchCount(): Result<Int> {
        return try {
            var newCount = 0
            dataStore.edit { prefs ->
                newCount = (prefs[LAUNCH_COUNT] ?: 0) + 1
                prefs[LAUNCH_COUNT] = newCount
            }
            Result.success(newCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTutorialSetting(key: String, enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                when (key) {
                    "gameplay" -> prefs[TUTORIAL_GAMEPLAY] = enabled
                    "hints" -> prefs[TUTORIAL_HINTS] = enabled
                    "themes" -> prefs[TUTORIAL_THEMES] = enabled
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
