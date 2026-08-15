package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.UserPreferences
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.usecase.MusicPlayer
import com.LetterQuest.domain.usecase.SoundPlayer
import com.LetterQuest.testutil.MainDispatcherRule
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
class SoundViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SoundViewModel
    private lateinit var mockSoundPlayer: MockSoundPlayer
    private lateinit var mockMusicPlayer: MockMusicPlayer
    private lateinit var mockRepository: MockPreferencesRepository

    @Before
    fun setup() {
        mockSoundPlayer = MockSoundPlayer()
        mockMusicPlayer = MockMusicPlayer()
        mockRepository = MockPreferencesRepository()
        viewModel = SoundViewModel(mockSoundPlayer, mockMusicPlayer, mockRepository)
    }

    @Test
    fun testSoundDefaultsToEnabled() {
        assertTrue(viewModel.isSoundEnabled.value)
    }

    @Test
    fun testSoundCanBeDisabled() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = false)
        )

        assertFalse(viewModel.isSoundEnabled.value)

        collectJob.cancel()
    }

    @Test
    fun testPlayCorrectGuessWhenEnabled() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = true)
        )

        viewModel.playCorrectGuess()

        assertTrue(mockSoundPlayer.correctGuessPlayed)

        collectJob.cancel()
    }

    @Test
    fun testPlayCorrectGuessWhenDisabled() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = false)
        )

        viewModel.playCorrectGuess()

        assertFalse(mockSoundPlayer.correctGuessPlayed)

        collectJob.cancel()
    }

    @Test
    fun testPlayIncorrectGuess() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = true)
        )

        viewModel.playIncorrectGuess()

        assertTrue(mockSoundPlayer.incorrectGuessPlayed)

        collectJob.cancel()
    }

    @Test
    fun testPlayWinSound() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = true)
        )

        viewModel.playWinSound()

        assertTrue(mockSoundPlayer.winSoundPlayed)

        collectJob.cancel()
    }

    @Test
    fun testPlayLoseSound() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSoundEnabled.collect { }
        }

        mockRepository.preferencesFlow.emit(
            UserPreferences(soundEnabled = true)
        )

        viewModel.playLoseSound()

        assertTrue(mockSoundPlayer.loseSoundPlayed)

        collectJob.cancel()
    }

    private class MockMusicPlayer : MusicPlayer {
        var started = false
        var released = false

        override fun start() { started = true }
        override fun stop() { started = false }
        override fun release() { released = true }
    }

    private class MockSoundPlayer : SoundPlayer {
        var correctGuessPlayed = false
        var incorrectGuessPlayed = false
        var winSoundPlayed = false
        var loseSoundPlayed = false
        var buttonClickPlayed = false

        override suspend fun playCorrectGuessSound() {
            correctGuessPlayed = true
        }

        override suspend fun playIncorrectGuessSound() {
            incorrectGuessPlayed = true
        }

        override suspend fun playWinSound() {
            winSoundPlayed = true
        }

        override suspend fun playLoseSound() {
            loseSoundPlayed = true
        }

        override suspend fun playButtonClickSound() {
            buttonClickPlayed = true
        }

        override fun release() {}
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
        override suspend fun setAdsRemoved(enabled: Boolean) = Result.success(Unit)
        override fun observeUnlockedThemes(): Flow<Set<String>> = MutableStateFlow(emptySet<String>()).asStateFlow()
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
