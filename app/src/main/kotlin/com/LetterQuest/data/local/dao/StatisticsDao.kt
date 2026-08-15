package com.LetterQuest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.LetterQuest.data.local.entity.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE id = 1")
    suspend fun getStatistics(): StatisticsEntity?

    @Query("SELECT * FROM statistics WHERE id = 1")
    fun observeStatistics(): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(statistics: StatisticsEntity)

    @Update
    suspend fun update(statistics: StatisticsEntity)

    @Query("UPDATE statistics SET gamesPlayed = gamesPlayed + 1, gamesWon = gamesWon + :won, gamesLost = gamesLost + :lost, totalScore = totalScore + :score, highestScore = MAX(highestScore, :score), averageScore = (totalScore + :score) / (gamesPlayed + 1), winRate = CAST(gamesWon + :won AS REAL) / (gamesPlayed + 1) WHERE id = 1")
    suspend fun atomicUpdate(won: Int, lost: Int, score: Int)

    @Query("DELETE FROM statistics")
    suspend fun deleteAll()
}
