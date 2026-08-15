package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.usecase.MusicPlayer
import com.LetterQuest.domain.usecase.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Gates sound-effect playback and background music behind the user's preferences.
 * Holds no Android framework types (`Context`, `MediaPlayer`), so it is safe in
 * plain JVM unit tests and cannot leak the application context.
 */
@HiltViewModel
class SoundViewModel @Inject constructor(
    private val soundPlayer: SoundPlayer,
    private val musicPlayer: MusicPlayer,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Eagerly: upstream starts immediately so .value is always up-to-date,
    // even when no composable is subscribed (which was the sound-toggle bug).
    val isSoundEnabled: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { it.soundEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isMusicEnabled: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { it.musicEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Whether the in-game hints / power-ups panel should be shown. */
    val isHintsEnabled: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { it.hintsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        viewModelScope.launch {
            isMusicEnabled.collect { enabled ->
                if (enabled) musicPlayer.start() else musicPlayer.stop()
            }
        }
    }

    fun playCorrectGuess() = playIfEnabled { playCorrectGuessSound() }
    fun playIncorrectGuess() = playIfEnabled { playIncorrectGuessSound() }
    fun playWinSound() = playIfEnabled { playWinSound() }
    fun playLoseSound() = playIfEnabled { playLoseSound() }
    fun playButtonClick() = playIfEnabled { playButtonClickSound() }
    // Hint sounds also go through the same sound-enabled gate
    fun playHintSound() = playIfEnabled { playButtonClickSound() }

    private inline fun playIfEnabled(crossinline block: suspend SoundPlayer.() -> Unit) {
        if (isSoundEnabled.value) viewModelScope.launch { soundPlayer.block() }
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
        soundPlayer.release()
    }
}

