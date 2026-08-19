package com.LetterQuest.domain.model

import java.util.UUID

data class GameHistoryEntry(
    val id: Int = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val word: String,
    val difficulty: Difficulty,
    val won: Boolean,
    val score: Int,
    val sessionScore: Int = 0,
    val guessedLetters: Set<Char>,
    val incorrectGuesses: Set<Char>,
    val elapsedSeconds: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String? = null
) {
    val totalGuesses: Int
        get() = guessedLetters.size + incorrectGuesses.size

    val correctGuesses: Int
        get() = guessedLetters.size
}
