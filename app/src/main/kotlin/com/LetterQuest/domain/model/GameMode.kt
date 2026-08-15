package com.LetterQuest.domain.model

/**
 * Core play-to-earn game modes selected on the setup screen.
 *
 * Distinct from [ChallengeMode], which describes promotional/special challenges; this
 * enum drives the per-run behaviour of [com.LetterQuest.ui.viewmodel.GameViewModel].
 */
enum class GameMode(val displayName: String, val description: String, val icon: String) {
    /** Level-based play: a clue is shown up front, and finishing fast earns 1–3 stars. */
    CLASSIC(
        displayName = "Classic",
        description = "Solve levels with a clue. Finish fast to earn up to 3 stars!",
        icon = "🎯"
    ),

    /** 60-second blitz: solve as many words as possible; each word pays out at the end. */
    TIMED(
        displayName = "Timed Blitz",
        description = "Guess as many words as you can in 60s. +2🪙 per word solved!",
        icon = "⏱️"
    );

    val isClassic: Boolean get() = this == CLASSIC
    val isTimed: Boolean get() = this == TIMED

    fun config(): GameModeConfig = when (this) {
        CLASSIC -> GameModeConfig(mode = this, showClueUpFront = true)
        TIMED -> GameModeConfig(
            mode = this,
            timeLimitSeconds = TIMED_SESSION_SECONDS,
            hintCostMultiplier = TIMED_HINT_COST_MULTIPLIER
        )
    }

    companion object {
        /** Length of one Timed Blitz run. */
        const val TIMED_SESSION_SECONDS: Long = 60L
        /** Hint multiplier applied in timed mode (hints are half price). */
        const val TIMED_HINT_COST_MULTIPLIER: Float = 0.5f
        /** Hint cost is never allowed to drop below this. */
        const val MIN_HINT_COST: Int = 1
        /** Tokens earned per word solved in timed mode, paid when the clock hits zero. */
        const val TOKENS_PER_TIMED_WORD: Int = 2
        /** Live score awarded per correct letter during a Timed Blitz session. */
        const val TIMED_POINTS_PER_CORRECT_LETTER: Int = 10
        /** Live score bonus awarded per completed word during a Timed Blitz session. */
        const val TIMED_POINTS_PER_WORD_SOLVED: Int = 50
    }
}

/**
 * Behavioural configuration for a [GameMode]. All fields have sensible defaults so
 * [GameMode.CLASSIC]'s config is a no-op relative to legacy gameplay.
 */
data class GameModeConfig(
    val mode: GameMode,
    val timeLimitSeconds: Long? = null,
    /** Scales [HintType.cost]; combined with [GameMode.MIN_HINT_COST] flooring. */
    val hintCostMultiplier: Float = 1f,
    /** Whether the word's clue is displayed in-game without spending a hint token. */
    val showClueUpFront: Boolean = false
) {
    /** Final price of a hint, after the multiplier and the floor. */
    fun effectiveHintCost(baseCost: Int): Int =
        maxOf(GameMode.MIN_HINT_COST, Math.round(baseCost * hintCostMultiplier))
}

/**
 * Star-rating thresholds for a Classic level, based on elapsed solve time.
 */
object StarRating {
    const val MAX_STARS = 3
    const val THREE_STAR_SECONDS: Long = 20L
    const val TWO_STAR_SECONDS: Long = 45L

    /** Returns 1–3 stars for a completed level; faster solves earn more stars. */
    fun forElapsedSeconds(seconds: Long): Int = when {
        seconds <= THREE_STAR_SECONDS -> 3
        seconds <= TWO_STAR_SECONDS -> 2
        else -> 1
    }.coerceIn(1, MAX_STARS)
}
