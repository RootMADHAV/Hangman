package com.hangman.domain.model

/**
 * How many consecutive days the player has completed the daily challenge.
 *
 * [current] resets to zero when a day is missed; [longest] is never reduced, so it
 * remains a record of the player's best run.
 */
data class DailyStreak(
    val current: Int = 0,
    val longest: Int = 0,
    val lastCompletedDateKey: String? = null
) {
    init {
        require(current >= 0) { "Current streak cannot be negative" }
        require(longest >= current) { "Longest streak cannot be below the current streak" }
    }
}
