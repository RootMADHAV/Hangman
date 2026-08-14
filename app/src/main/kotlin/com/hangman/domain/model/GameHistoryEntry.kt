package com.hangman.domain.model

data class GameHistoryEntry(
    val id: Int = 0,
    val word: String,
    val difficulty: Difficulty,
    val won: Boolean,
    val score: Int,
    val guessedLetters: Set<Char>,
    val incorrectGuesses: Set<Char>,
    val elapsedSeconds: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val category: String? = null
) {
    val totalGuesses: Int
        get() = guessedLetters.size + incorrectGuesses.size

    val correctGuesses: Int
        get() = guessedLetters.size
}
