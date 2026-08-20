package com.LetterQuest.domain.model

import com.LetterQuest.domain.model.GameMode
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
    val category: String? = null,
    val hintsUsed: Int = 0,
    val gameMode: String = GameMode.CLASSIC.name
) {
    val totalGuesses: Int
        get() = guessedLetters.size + incorrectGuesses.size

    val correctGuesses: Int
        get() = guessedLetters.size
}
