package com.LetterQuest.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.GameMode

@Entity(tableName = "game_history", indices = [Index(value = ["uuid"], unique = true)])
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val word: String,
    val difficulty: String,
    val won: Boolean,
    val score: Int,
    val sessionScore: Int = 0,
    val guessedLetters: String, // comma-separated
    val incorrectGuesses: String, // comma-separated
    val elapsedSeconds: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String? = null,
    val hintsUsed: Int = 0,
    val gameMode: String = GameMode.CLASSIC.name
) {
    fun toGameHistoryEntry(): GameHistoryEntry {
        return GameHistoryEntry(
            id = id,
            uuid = uuid,
            word = word,
            difficulty = Difficulty.valueOf(difficulty),
            won = won,
            score = score,
            sessionScore = sessionScore,
            guessedLetters = if (guessedLetters.isEmpty()) emptySet() else guessedLetters.split(",").map { it.single() }.toSet(),
            incorrectGuesses = if (incorrectGuesses.isEmpty()) emptySet() else incorrectGuesses.split(",").map { it.single() }.toSet(),
            elapsedSeconds = elapsedSeconds,
            playedAt = playedAt,
            updatedAt = updatedAt,
            category = category,
            hintsUsed = hintsUsed,
            gameMode = gameMode
        )
    }

    companion object {
        fun fromGameHistoryEntry(entry: GameHistoryEntry): GameHistoryEntity {
            return GameHistoryEntity(
                id = entry.id,
                uuid = entry.uuid,
                word = entry.word,
                difficulty = entry.difficulty.name,
                won = entry.won,
                score = entry.score,
                sessionScore = entry.sessionScore,
                guessedLetters = entry.guessedLetters.joinToString(","),
                incorrectGuesses = entry.incorrectGuesses.joinToString(","),
                elapsedSeconds = entry.elapsedSeconds,
                playedAt = entry.playedAt,
                updatedAt = entry.updatedAt,
                category = entry.category,
                hintsUsed = entry.hintsUsed,
                gameMode = entry.gameMode
            )
        }
    }
}
