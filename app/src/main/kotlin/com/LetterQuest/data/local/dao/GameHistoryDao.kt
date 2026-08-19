package com.LetterQuest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.LetterQuest.data.local.entity.GameHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {

    @Insert
    suspend fun insertGame(game: GameHistoryEntity)

    @Query("SELECT * FROM game_history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentGames(limit: Int): List<GameHistoryEntity>

    @Query("SELECT * FROM game_history ORDER BY playedAt DESC")
    suspend fun getAllGames(): List<GameHistoryEntity>

    @Query("SELECT * FROM game_history ORDER BY playedAt DESC")
    fun observeAllGames(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history WHERE won = 1 ORDER BY playedAt DESC")
    suspend fun getWonGames(): List<GameHistoryEntity>

    @Query("SELECT * FROM game_history WHERE won = 0 ORDER BY playedAt DESC")
    suspend fun getLostGames(): List<GameHistoryEntity>

    @Query("SELECT COUNT(*) FROM game_history WHERE won = 1 AND playedAt >= :fromTime ORDER BY playedAt DESC")
    suspend fun getConsecutiveWins(fromTime: Long): Int

    @Query("DELETE FROM game_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun getGameCount(): Int

    @Query("SELECT * FROM game_history WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): GameHistoryEntity?

    @Query("UPDATE game_history SET score = :score, sessionScore = :sessionScore, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun updateScore(uuid: String, score: Int, sessionScore: Int, updatedAt: Long)

    @Query("INSERT OR REPLACE INTO game_history (uuid, word, difficulty, won, score, sessionScore, guessedLetters, incorrectGuesses, elapsedSeconds, playedAt, updatedAt, category) VALUES (:uuid, :word, :difficulty, :won, :score, :sessionScore, :guessedLetters, :incorrectGuesses, :elapsedSeconds, :playedAt, :updatedAt, :category)")
    suspend fun upsertByUuid(uuid: String, word: String, difficulty: String, won: Boolean, score: Int, sessionScore: Int, guessedLetters: String, incorrectGuesses: String, elapsedSeconds: Long, playedAt: Long, updatedAt: Long, category: String?)
}
