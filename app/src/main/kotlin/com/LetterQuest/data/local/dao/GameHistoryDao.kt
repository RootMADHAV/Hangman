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
}
