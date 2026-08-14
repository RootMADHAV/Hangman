package com.hangman.domain.model

enum class Difficulty(val maxAttempts: Int, val wordLength: IntRange) {
    /** Long words: more time, many letters to spot. */
    EASY(10, 8..15),
    /** Medium-length words with moderate commonness. */
    MEDIUM(8, 6..9),
    /** Short, sharp words — fewer attempts to get it right. */
    HARD(6, 4..6)
}
