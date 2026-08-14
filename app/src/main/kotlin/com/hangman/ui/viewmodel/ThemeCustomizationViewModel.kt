package com.hangman.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hangman.domain.model.ThemePreset
import com.hangman.domain.model.UserTokens
import com.hangman.domain.repository.PreferencesRepository
import com.hangman.domain.repository.TokenRepository
import com.hangman.ui.theme.ColorPresets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemePresetEntry(
    val preset: ThemePreset,
    val isUnlocked: Boolean,
    val isAffordable: Boolean
)

data class ThemeCustomizationUIState(
    val presets: List<ThemePresetEntry> = emptyList(),
    val selectedPresetId: String = "light",
    val tokenBalance: Int = 0,
    val message: String? = null
)

@HiltViewModel
class ThemeCustomizationViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ThemeCustomizationUIState> = combine(
        preferencesRepository.observePreferences(),
        preferencesRepository.observeUnlockedThemes(),
        tokenRepository.observeTokens(),
        _message
    ) { prefs, unlocked, tokens, msg ->
        val selectedId = prefs.themePresetId ?: "light"
        ThemeCustomizationUIState(
            presets = ColorPresets.allPresets.map { preset ->
                ThemePresetEntry(
                    preset = preset,
                    isUnlocked = preset.id in unlocked,
                    isAffordable = tokens.canAfford(preset.cost)
                )
            },
            selectedPresetId = selectedId,
            tokenBalance = tokens.balance,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ThemeCustomizationUIState()
    )

    fun selectPreset(presetId: String) {
        viewModelScope.launch {
            preferencesRepository.setThemePreset(presetId)
        }
    }

    fun purchaseAndApply(preset: ThemePreset) {
        viewModelScope.launch {
            tokenRepository.spendTokens(preset.cost)
                .onSuccess {
                    preferencesRepository.unlockTheme(preset.id)
                    preferencesRepository.setThemePreset(preset.id)
                    _message.value = "🎨 ${preset.name} unlocked!"
                }
                .onFailure {
                    _message.value = "Not enough tokens — need 🪙 ${preset.cost}"
                }
        }
    }

    fun clearMessage() { _message.value = null }
}
