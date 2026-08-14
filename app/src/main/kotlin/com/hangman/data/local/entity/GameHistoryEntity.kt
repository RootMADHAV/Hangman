package com.hangman.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameHistoryEntry

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val difficulty: String,
    val won: Boolean,
    val score: Int,
    val guessedLetters: String, // comma-separated
    val incorrectGuesses: String, // comma-separated
    val elapsedSeconds: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val category: String? = null
) {
    fun toGameHistoryEntry(): GameHistoryEntry {
        return GameHistoryEntry(
            id = id,
            word = word,
            difficulty = Difficulty.valueOf(difficulty),
            won = won,
            score = score,
            guessedLetters = if (guessedLetters.isEmpty()) emptySet() else guessedLetters.split(",").map { it.single() }.toSet(),
            incorrectGuesses = if (incorrectGuesses.isEmpty()) emptySet() else incorrectGuesses.split(",").map { it.single() }.toSet(),
            elapsedSeconds = elapsedSeconds,
            playedAt = playedAt,
            category = category
        )
    }

    companion object {
        fun fromGameHistoryEntry(entry: GameHistoryEntry): GameHistoryEntity {
            return GameHistoryEntity(
                id = entry.id,
                word = entry.word,
                difficulty = entry.difficulty.name,
                won = entry.won,
                score = entry.score,
                guessedLetters = entry.guessedLetters.joinToString(","),
                incorrectGuesses = entry.incorrectGuesses.joinToString(","),
                elapsedSeconds = entry.elapsedSeconds,
                playedAt = entry.playedAt,
                category = entry.category
            )
        }
    }
}
