package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.UserPreferences
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.ui.theme.ColorPresets
import com.LetterQuest.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeCustomizationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun makeViewModel(
        prefs: UserPreferences = UserPreferences(themePresetId = "light"),
        unlockedThemes: Set<String> = ColorPresets.FREE_THEME_IDS,
        balance: Int = 500
    ): Triple<ThemeCustomizationViewModel, MockPreferencesRepository, MockTokenRepository> {
        val mockPrefs = MockPreferencesRepository(prefs, unlockedThemes)
        val mockTokens = MockTokenRepository(balance)
        val vm = ThemeCustomizationViewModel(mockPrefs, mockTokens)
        return Triple(vm, mockPrefs, mockTokens)
    }

    @Test
    fun uiState_emitsAllPresets() = runTest {
        val (vm) = makeViewModel()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        assertEquals(ColorPresets.allPresets.size, vm.uiState.value.presets.size)
        job.cancel()
    }

    @Test
    fun uiState_freePresetsAreUnlocked() = runTest {
        val (vm) = makeViewModel()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        val freeIds = ColorPresets.FREE_THEME_IDS
        vm.uiState.value.presets
            .filter { it.preset.id in freeIds }
            .forEach { assertTrue("${it.preset.id} should be unlocked", it.isUnlocked) }
        job.cancel()
    }

    @Test
    fun uiState_premiumPresetsLockedByDefault() = runTest {
        val (vm) = makeViewModel(unlockedThemes = ColorPresets.FREE_THEME_IDS)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        val premiumLocked = vm.uiState.value.presets
            .filter { it.preset.cost > 0 && !it.isUnlocked }
        assertTrue(premiumLocked.isNotEmpty())
        job.cancel()
    }

    @Test
    fun uiState_reflectsSelectedPreset() = runTest {
        val (vm) = makeViewModel(prefs = UserPreferences(themePresetId = "dark"))
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        assertEquals("dark", vm.uiState.value.selectedPresetId)
        job.cancel()
    }

    @Test
    fun uiState_defaultsToLightWhenNoPresetSet() = runTest {
        val (vm) = makeViewModel(prefs = UserPreferences(themePresetId = null))
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        assertEquals("light", vm.uiState.value.selectedPresetId)
        job.cancel()
    }

    @Test
    fun selectPreset_savesToRepository() = runTest {
        val (vm, mockPrefs) = makeViewModel()
        vm.selectPreset("dark")
        assertEquals("dark", mockPrefs.savedThemePresetId)
    }

    @Test
    fun purchaseAndApply_deductsTokensAndUnlocks() = runTest {
        val (vm, mockPrefs, mockTokens) = makeViewModel(balance = 500)
        val premiumPreset = ColorPresets.allPresets.first { it.cost > 0 }
        vm.purchaseAndApply(premiumPreset)
        assertTrue(mockTokens.spentAmount > 0)
        assertTrue(mockPrefs.unlockedThemeIds.contains(premiumPreset.id))
    }

    @Test
    fun purchaseAndApply_failsWhenInsufficientTokens() = runTest {
        val (vm, _, _) = makeViewModel(balance = 0)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        val premiumPreset = ColorPresets.allPresets.first { it.cost > 0 }
        vm.purchaseAndApply(premiumPreset)
        assertTrue(vm.uiState.value.message?.contains("Not enough") == true)
        job.cancel()
    }

    // ── Mocks ──────────────────────────────────────────────────────────────────

    private class MockPreferencesRepository(
        initialPreferences: UserPreferences,
        initialUnlocked: Set<String>
    ) : PreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)
        private val unlockedFlow = MutableStateFlow(initialUnlocked)
        var savedThemePresetId: String? = null
        val unlockedThemeIds: MutableSet<String> = initialUnlocked.toMutableSet()

        override fun observePreferences(): Flow<UserPreferences> = preferencesFlow.asStateFlow()
        override fun observeUnlockedThemes(): Flow<Set<String>> = unlockedFlow.asStateFlow()
        override suspend fun getPreferences() = Result.success(preferencesFlow.value)
        override suspend fun setSoundEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setMusicEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setDarkTheme(enabled: Boolean) = Result.success(Unit)
        override suspend fun setDefaultDifficulty(difficulty: Difficulty) = Result.success(Unit)
        override suspend fun setNotificationsEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setHintsEnabled(enabled: Boolean) = Result.success(Unit)
        override suspend fun setAdsRemoved(enabled: Boolean) = Result.success(Unit)
        override suspend fun setThemePreset(presetId: String): Result<Unit> {
            savedThemePresetId = presetId
            preferencesFlow.value = preferencesFlow.value.copy(themePresetId = presetId)
            return Result.success(Unit)
        }
        override suspend fun unlockTheme(themeId: String): Result<Unit> {
            unlockedThemeIds.add(themeId)
            unlockedFlow.value = unlockedThemeIds.toSet()
            return Result.success(Unit)
        }
        override suspend fun getTutorialSettings() = Result.success(com.LetterQuest.domain.model.TutorialSettings())
        override suspend fun setTutorialSetting(key: String, enabled: Boolean) = Result.success(Unit)
        override fun observeTutorialSettings(): Flow<com.LetterQuest.domain.model.TutorialSettings> = MutableStateFlow(com.LetterQuest.domain.model.TutorialSettings())
        override suspend fun setTutorialSeen(type: String) = Result.success(Unit)
        override fun observeLaunchCount(): Flow<Int> = MutableStateFlow(0)
        override suspend fun getLaunchCount() = Result.success(0)
        override suspend fun incrementLaunchCount() = Result.success(1)
    }

    private class MockTokenRepository(initialBalance: Int) : TokenRepository {
        private val tokensFlow = MutableStateFlow(UserTokens(initialBalance))
        var spentAmount = 0

        override fun observeTokens(): Flow<UserTokens> = tokensFlow.asStateFlow()
        override suspend fun getTokens() = Result.success(tokensFlow.value)
        override suspend fun spendTokens(amount: Int): Result<UserTokens> {
            val current = tokensFlow.value
            return if (current.canAfford(amount)) {
                spentAmount += amount
                val updated = UserTokens(current.balance - amount)
                tokensFlow.value = updated
                Result.success(updated)
            } else {
                Result.failure(IllegalStateException("Not enough tokens"))
            }
        }
        override suspend fun earnTokens(amount: Int): Result<UserTokens> {
            val updated = UserTokens(tokensFlow.value.balance + amount)
            tokensFlow.value = updated
            return Result.success(updated)
        }
        override suspend fun reset() {
            tokensFlow.value = UserTokens(0)
        }
    }
}
