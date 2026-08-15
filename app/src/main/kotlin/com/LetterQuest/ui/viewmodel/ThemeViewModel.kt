package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.ThemeColors
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.ui.theme.ColorPresets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { it.darkTheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    val customColors: StateFlow<ThemeColors?> = preferencesRepository
        .observePreferences()
        .map { prefs ->
            prefs.themePresetId?.let { presetId ->
                ColorPresets.getPresetById(presetId)?.colors
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    /** Whether the currently applied [customColors] preset is a dark scene. */
    val customIsDark: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { prefs ->
            prefs.themePresetId?.let { presetId ->
                ColorPresets.getPresetById(presetId)?.isDark
            } ?: false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )
}
