package com.hangman.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A once-per-day puzzle drawn deterministically from the catalog.
 *
 * The word is derived from the date itself rather than stored, so the same day always
 * yields the same puzzle without needing to persist it. Only completion state is saved.
 */
data class DailyChallenge(
    val dateKey: String,
    val word: Word,
    val isCompleted: Boolean = false,
    val wasWon: Boolean = false
) {
    companion object {
        private val KEY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        /** Bonus paid on top of the normal win reward, to make the daily worth returning for. */
        const val COMPLETION_BONUS_TOKENS = 20

        fun dateKeyFor(date: LocalDate): String = date.format(KEY_FORMAT)

        /**
         * Picks an index into a list of [size] for [dateKey]. Deterministic: the same
         * date always maps to the same index, so a player cannot reroll by restarting.
         */
        fun indexFor(dateKey: String, size: Int): Int {
            require(size > 0) { "Cannot select from an empty list" }
            // Multiply before masking so adjacent dates don't map to adjacent indices.
            val hash = dateKey.hashCode() * 2654435761L
            return ((hash ushr 16) % size).toInt().let { if (it < 0) it + size else it }
        }
    }
}
