package com.hangman.domain.usecase

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameState
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.StarRating

object ScoreCalculator {

    private const val BASE_SCORE = 100
    private const val PER_LETTER_BONUS = 10
    private const val PER_ATTEMPT_BONUS = 5

    /**
     * @param scoreMultiplier applied last, for perks such as
     *   [com.hangman.domain.model.ShopItem.SCORE_BOOST]. 1f leaves the score unchanged.
     */
    fun calculateScore(gameStatus: GameStatus, scoreMultiplier: Float = 1f): Int {
        if (gameStatus.state != GameState.WON) {
            return 0
        }

        val wordLengthBonus = gameStatus.word.normalizedValue.length * PER_LETTER_BONUS
        val remainingAttemptsBonus = gameStatus.remainingAttempts * PER_ATTEMPT_BONUS
        val subtotal = BASE_SCORE + wordLengthBonus + remainingAttemptsBonus

        // Rounded rather than truncated: the previous Int conversion collapsed the 1.5x
        // MEDIUM multiplier to 1x, making MEDIUM score identically to EASY.
        return Math.round(subtotal * multiplierFor(gameStatus.word.difficulty) * scoreMultiplier)
    }

    private fun multiplierFor(difficulty: Difficulty): Float = when (difficulty) {
        Difficulty.EASY -> 1.0f
        Difficulty.MEDIUM -> 1.5f
        Difficulty.HARD -> 2.0f
    }

    fun calculateScoreProgression(
        wordLength: Int,
        remainingAttempts: Int,
        difficultyMultiplier: Float = 1.0f
    ): Int {
        val lengthBonus = wordLength * PER_LETTER_BONUS
        val attemptBonus = remainingAttempts * PER_ATTEMPT_BONUS
        return Math.round((BASE_SCORE + lengthBonus + attemptBonus) * difficultyMultiplier)
    }

    /**
     * Star-rating (1–3) for a won Classic level, based purely on the elapsed solve
     * time — the decisive factor the user asked the grade to reflect.
     *
     * See [com.hangman.domain.model.StarRating] for the thresholds.
     */
    fun calculateStarRating(gameStatus: GameStatus): Int {
        if (gameStatus.state != GameState.WON) return 0
        return StarRating.forElapsedSeconds(gameStatus.elapsedSeconds)
    }
}
