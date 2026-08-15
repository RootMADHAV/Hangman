package com.LetterQuest.data.repository

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.LetterQuest.domain.model.UserPreferences

class MockPreferencesRepositoryTest {

    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        repository = MockPreferencesRepository()
    }

    @Test
    fun testObservePreferences() = runTest {
        val preferences = repository.getPreferences().getOrNull()
        assertNotNull(preferences)
        assertTrue(preferences?.soundEnabled == true)
    }

    @Test
    fun testSetSoundEnabled() = runTest {
        val result = repository.setSoundEnabled(false)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetDarkTheme() = runTest {
        val result = repository.setDarkTheme(true)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetDefaultDifficulty() = runTest {
        val result = repository.setDefaultDifficulty(Difficulty.HARD)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetNotificationsEnabled() = runTest {
        val result = repository.setNotificationsEnabled(false)
        assertTrue(result.isSuccess)
    }

    private class MockPreferencesRepository : PreferencesRepository {
        private val preferencesState = MutableStateFlow(UserPreferences())

        override fun observePreferences(): Flow<UserPreferences> = preferencesState

        override suspend fun getPreferences(): Result<UserPreferences> {
            return Result.success(preferencesState.value)
        }

        override suspend fun setSoundEnabled(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(soundEnabled = enabled)
            return Result.success(Unit)
        }

        override suspend fun setMusicEnabled(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(musicEnabled = enabled)
            return Result.success(Unit)
        }

        override suspend fun setDarkTheme(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(darkTheme = enabled)
            return Result.success(Unit)
        }

        override suspend fun setDefaultDifficulty(difficulty: Difficulty): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(defaultDifficulty = difficulty)
            return Result.success(Unit)
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(notificationsEnabled = enabled)
            return Result.success(Unit)
        }

        override suspend fun setHintsEnabled(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(hintsEnabled = enabled)
            return Result.success(Unit)
        }

        override suspend fun setAdsRemoved(enabled: Boolean): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(adsRemoved = enabled)
            return Result.success(Unit)
        }

        override suspend fun setThemePreset(presetId: String): Result<Unit> {
            preferencesState.value = preferencesState.value.copy(themePresetId = presetId)
            return Result.success(Unit)
        }

        override fun observeUnlockedThemes(): Flow<Set<String>> = MutableStateFlow(emptySet<String>())
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
