package com.LetterQuest.domain.model

import android.os.SystemClock

data class GameStatus(
    val word: Word,
    val guessedLetters: Set<Char> = emptySet(),
    val incorrectGuesses: Set<Char> = emptySet(),
    val remainingAttempts: Int = word.difficulty.maxAttempts,
    val state: GameState = GameState.PLAYING,
    val score: Int = 0,
    val gameStartTime: Long = SystemClock.elapsedRealtime(),
    val gameEndTime: Long? = null,
    val isPaused: Boolean = false,
    val pauseStartTime: Long? = null,
    val totalPausedMillis: Long = 0L,
    /** Consecutive correct guesses without an incorrect one in between. */
    val currentCombo: Int = 0,
    /** Highest combo reached this game — used to calculate a bonus reward at win. */
    val maxCombo: Int = 0,
    /** The mode this game is being played in. */
    val mode: GameMode = GameMode.CLASSIC,
    /** Remaining seconds on the clock; only meaningful in [GameMode.TIMED]. */
    val timeRemainingSeconds: Long? = null,
    /** 1-based level counter for Classic mode; advances on each win. */
    val levelIndex: Int = 1,
    /** Whether the word's clue is currently visible without spending a hint. */
    val showClue: Boolean = false,
    /** Words completed so far in this run (timed mode: words within one session). */
    val timedWordsSolved: Int = 0,
    /** Star rating (1–3) computed when a Classic level is won; null until then. */
    val approvedStarRating: Int? = null,
    /** Perks activated for this game. */
    val perks: Set<ShopItem> = emptySet()
) {
    val revealedWord: String
        get() = word.normalizedValue.map { letter ->
            when {
                letter.isWhitespace() -> '/'
                letter in guessedLetters -> letter
                else -> '_'
            }
        }.joinToString("")

    val isGameOver: Boolean
        get() = state != GameState.PLAYING

    val totalGuesses: Int
        get() = guessedLetters.size + incorrectGuesses.size

    val correctGuesses: Set<Char>
        get() = guessedLetters intersect word.normalizedValue.toSet()

    val elapsedMillis: Long
        get() {
            val currentTime = gameEndTime ?: SystemClock.elapsedRealtime()
            val currentPause = if (isPaused && pauseStartTime != null) currentTime - pauseStartTime else 0L
            return (currentTime - gameStartTime - totalPausedMillis - currentPause).coerceAtLeast(0L)
        }

    val elapsedSeconds: Long
        get() = elapsedMillis / 1000

    init {
        require(remainingAttempts >= 0) { "Remaining attempts cannot be negative" }
    }

    fun isLetterGuessed(letter: Char): Boolean {
        return letter.uppercaseChar() in guessedLetters || letter.uppercaseChar() in incorrectGuesses
    }

    fun isWordComplete(): Boolean {
        return word.normalizedValue.all { it.isWhitespace() || it in guessedLetters }
    }

    /**
     * Clue to display up-front for this word, respecting [showClue] and [mode].
     * Returns null when no clue exists or it should stay hidden (timed mode before
     * a hint is bought).
     */
    val visibleClue: String?
        get() = when {
            !showClue -> null
            word.hint.isNullOrBlank() -> null
            else -> word.hint
        }
}
