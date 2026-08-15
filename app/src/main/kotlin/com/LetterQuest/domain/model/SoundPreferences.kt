package com.LetterQuest.domain.model

data class SoundPreferences(
    val soundEffectsEnabled: Boolean = true,
    val correctGuessEnabled: Boolean = true,
    val incorrectGuessEnabled: Boolean = true,
    val winEnabled: Boolean = true,
    val loseEnabled: Boolean = true,
    val buttonClickEnabled: Boolean = true,
    val milestoneEnabled: Boolean = true,
    val levelUpEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = true,
    val volume: Float = 1f
) {
    fun isSoundEnabled(soundType: SoundType): Boolean {
        return soundEffectsEnabled && when (soundType) {
            SoundType.CORRECT_GUESS -> correctGuessEnabled
            SoundType.INCORRECT_GUESS -> incorrectGuessEnabled
            SoundType.WIN -> winEnabled
            SoundType.LOSE -> loseEnabled
            SoundType.BUTTON_CLICK -> buttonClickEnabled
            SoundType.MILESTONE -> milestoneEnabled
            SoundType.LEVEL_UP -> levelUpEnabled
            SoundType.BACKGROUND_MUSIC -> backgroundMusicEnabled
        }
    }
}

enum class SoundType {
    CORRECT_GUESS,
    INCORRECT_GUESS,
    WIN,
    LOSE,
    BUTTON_CLICK,
    MILESTONE,
    LEVEL_UP,
    BACKGROUND_MUSIC;

    val displayName: String
        get() = when (this) {
            CORRECT_GUESS -> "Correct Guess"
            INCORRECT_GUESS -> "Incorrect Guess"
            WIN -> "Win Sound"
            LOSE -> "Lose Sound"
            BUTTON_CLICK -> "Button Click"
            MILESTONE -> "Milestone"
            LEVEL_UP -> "Level Up"
            BACKGROUND_MUSIC -> "Background Music"
        }

    val icon: String
        get() = when (this) {
            CORRECT_GUESS -> "✓"
            INCORRECT_GUESS -> "✗"
            WIN -> "🎉"
            LOSE -> "😢"
            BUTTON_CLICK -> "🔘"
            MILESTONE -> "🏆"
            LEVEL_UP -> "⬆️"
            BACKGROUND_MUSIC -> "🎵"
        }
}
