package com.LetterQuest.domain.model

enum class ChallengeMode {
    CLASSIC,
    TIMED,
    LIMITED_GUESSES,
    CATEGORY_CHALLENGE;

    val displayName: String
        get() = when (this) {
            CLASSIC -> "Classic"
            TIMED -> "Timed"
            LIMITED_GUESSES -> "Limited Guesses"
            CATEGORY_CHALLENGE -> "Category Challenge"
        }

    val description: String
        get() = when (this) {
            CLASSIC -> "Play at your own pace"
            TIMED -> "Complete within time limit"
            LIMITED_GUESSES -> "Fewer attempts available"
            CATEGORY_CHALLENGE -> "Guess from one category only"
        }

    val icon: String
        get() = when (this) {
            CLASSIC -> "🎮"
            TIMED -> "⏱️"
            LIMITED_GUESSES -> "❌"
            CATEGORY_CHALLENGE -> "📂"
        }
}

data class ChallengeModeConfig(
    val mode: ChallengeMode,
    val timeLimit: Long? = null,
    val maxGuesses: Int? = null,
    val selectedCategory: String? = null,
    val scoreMultiplier: Float = 1f
)

/**
 * Maps a legacy [ChallengeMode] onto the runtime [GameMode] used by the game
 * view-model. Limited/category challenges are still backed by Classic levels
 * underneath (the challenge rules layer on top).
 */
fun ChallengeMode.toGameMode(): GameMode = when (this) {
    ChallengeMode.TIMED -> GameMode.TIMED
    else -> GameMode.CLASSIC
}
