package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.UserPreferences
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockPreferencesRepository: MockPreferencesRepository

    @Before
    fun setup() {
        mockPreferencesRepository = MockPreferencesRepository()
        viewModel = SettingsViewModel(mockPreferencesRepository)
    }

    @Test
    fun testInitialPreferences() {
        val preferences = viewModel.preferences.value
        assertTrue(preferences.soundEnabled)
        assertFalse(preferences.darkTheme)
        assertEquals(Difficulty.MEDIUM, preferences.defaultDifficulty)
        assertTrue(preferences.notificationsEnabled)
    }

    @Test
    fun testSetSoundEnabled() = runTest {
        viewModel.setSoundEnabled(false)
        assertTrue(mockPreferencesRepository.lastSoundEnabled == false)
    }

    @Test
    fun testSetDarkTheme() = runTest {
        viewModel.setDarkTheme(true)
        assertTrue(mockPreferencesRepository.lastDarkTheme == true)
    }

    @Test
    fun testSetNotificationsEnabled() = runTest {
        viewModel.setNotificationsEnabled(false)
        assertTrue(mockPreferencesRepository.lastNotificationsEnabled == false)
    }

    private class MockPreferencesRepository : PreferencesRepository {
        private val preferencesFlow = MutableSharedFlow<UserPreferences>(replay = 1)
        var lastSoundEnabled: Boolean? = null
        var lastMusicEnabled: Boolean? = null
        var lastDarkTheme: Boolean? = null
        var lastHintsEnabled: Boolean? = null
        var lastNotificationsEnabled: Boolean? = null

        init {
            preferencesFlow.tryEmit(UserPreferences())
        }

        override fun observePreferences(): Flow<UserPreferences> = preferencesFlow.asSharedFlow()

        override suspend fun getPreferences(): Result<UserPreferences> {
            return Result.success(UserPreferences())
        }

        override suspend fun setSoundEnabled(enabled: Boolean): Result<Unit> {
            lastSoundEnabled = enabled
            return Result.success(Unit)
        }

        override suspend fun setMusicEnabled(enabled: Boolean): Result<Unit> {
            lastMusicEnabled = enabled
            return Result.success(Unit)
        }

        override suspend fun setDarkTheme(enabled: Boolean): Result<Unit> {
            lastDarkTheme = enabled
            return Result.success(Unit)
        }

        override suspend fun setHintsEnabled(enabled: Boolean): Result<Unit> {
            lastHintsEnabled = enabled
            return Result.success(Unit)
        }

        override suspend fun setAdsRemoved(enabled: Boolean): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
            lastNotificationsEnabled = enabled
            return Result.success(Unit)
        }

        override suspend fun setThemePreset(presetId: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun setDefaultDifficulty(difficulty: Difficulty): Result<Unit> {
            return Result.success(Unit)
        }

        override fun observeUnlockedThemes(): Flow<Set<String>> = MutableStateFlow(emptySet<String>()).asSharedFlow()
        override suspend fun unlockTheme(themeId: String) = Result.success(Unit)
        override suspend fun getTutorialSettings() = Result.success(com.LetterQuest.domain.model.TutorialSettings())
        override suspend fun setTutorialSetting(key: String, enabled: Boolean) = Result.success(Unit)
        override fun observeTutorialSettings(): Flow<com.LetterQuest.domain.model.TutorialSettings> = MutableStateFlow(com.LetterQuest.domain.model.TutorialSettings())
        override suspend fun setTutorialSeen(type: String) = Result.success(Unit)
        override fun observeLaunchCount(): Flow<Int> = MutableStateFlow(0)
        override suspend fun getLaunchCount() = Result.success(0)
        override suspend fun incrementLaunchCount() = Result.success(1)
    }
}
