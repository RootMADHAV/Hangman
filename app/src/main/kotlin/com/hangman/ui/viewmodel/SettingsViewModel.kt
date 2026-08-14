package com.hangman.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.TutorialSettings
import com.hangman.domain.model.UserPreferences
import com.hangman.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository
        .observePreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UserPreferences()
        )

    val tutorialSettings: StateFlow<TutorialSettings> = preferencesRepository
        .observeTutorialSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = TutorialSettings()
        )

    val launchCount: StateFlow<Int> = preferencesRepository
        .observeLaunchCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setMusicEnabled(enabled) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkTheme(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }

    fun setHintsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setHintsEnabled(enabled) }
    }

    fun setTutorialSetting(key: String, enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setTutorialSetting(key, enabled) }
    }

    fun markTutorialSeen(type: String) {
        viewModelScope.launch { preferencesRepository.setTutorialSeen(type) }
    }
}
