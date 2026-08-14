package com.hangman.ui.viewmodel

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.UserPreferences
import com.hangman.domain.repository.PreferencesRepository
import com.hangman.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ThemeViewModel
    private lateinit var mockRepository: MockPreferencesRepository

    @Before
    fun setup() {
        mockRepository = MockPreferencesRepository()
        viewModel = ThemeViewModel(mockRepository)
    }

    @Test
    fun testThemeDefaultsToLight() {
        assertFalse(viewModel.isDarkTheme.value)
    }

    @Test
    fun testThemeChanges() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isDarkTheme.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(darkTheme = true)
        )

        assertTrue(viewModel.isDarkTheme.value)

        collectJob.cancel()
    }

    @Test
    fun testThemeToggle() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isDarkTheme.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(darkTheme = false)
        )
        assertFalse(viewModel.isDarkTheme.value)

        mockRepository.preferencesFlow.emit(
            UserPreferences(darkTheme = true)
        )
        assertTrue(viewModel.isDarkTheme.value)

        collectJob.cancel()
    }

    @Test
    fun testCustomColorsDefaultsToNull() {
        assertTrue(viewModel.customColors.value == null)
    }

    @Test
    fun testCustomColorsUpdateWithPreset() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.customColors.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(themePresetId = "light")
        )

        assertTrue(viewModel.customColors.value != null)

        collectJob.cancel()
    }

    private class MockPreferencesRepository : PreferencesRepository {
        val preferencesFlow = MutableStateFlow(UserPreferences())

        override fun observePreferences(): Flow<UserPreferences> = preferencesFlow.asStateFlow()
        override suspend fun getPreferences() = Result.success(UserPreferences())
        override suspend fun setSoundEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setMusicEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setDarkTheme(enabled: Boolean) = Result.success(Unit)
        override suspend fun setDefaultDifficulty(difficulty: Difficulty) = Result.success(Unit)
        override suspend fun setNotificationsEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setThemePreset(presetId: String) = Result.success(Unit)
        override suspend fun setHintsEnabled(enabled: Boolean) = Result.success(Unit)
        override fun observeUnlockedThemes(): Flow<Set<String>> = MutableStateFlow(emptySet<String>()).asStateFlow()
        override suspend fun unlockTheme(themeId: String) = Result.success(Unit)
        override suspend fun getTutorialSettings() = Result.success(com.hangman.domain.model.TutorialSettings())
        override suspend fun setTutorialSetting(key: String, enabled: Boolean) = Result.success(Unit)
        override fun observeTutorialSettings(): Flow<com.hangman.domain.model.TutorialSettings> = MutableStateFlow(com.hangman.domain.model.TutorialSettings())
        override suspend fun setTutorialSeen(type: String) = Result.success(Unit)
        override fun observeLaunchCount(): Flow<Int> = MutableStateFlow(0)
        override suspend fun getLaunchCount() = Result.success(0)
        override suspend fun incrementLaunchCount() = Result.success(1)
    }
}
