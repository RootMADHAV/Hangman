package com.hangman.data.local.repository

import com.hangman.data.local.dao.StatisticsDao
import com.hangman.data.local.entity.StatisticsEntity
import com.hangman.domain.model.PlayerStatistics
import com.hangman.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryLocal @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository {

    override suspend fun getStatistics(): Result<PlayerStatistics> {
        return try {
            val entity = statisticsDao.getStatistics()
                ?: return Result.success(PlayerStatistics())
            Result.success(entity.toPlayerStatistics())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeStatistics(): Flow<PlayerStatistics> {
        return statisticsDao.observeStatistics()
            .map { entity ->
                entity?.toPlayerStatistics() ?: PlayerStatistics()
            }
    }

    override suspend fun updateStatistics(statistics: PlayerStatistics): Result<Unit> {
        return try {
            val entity = StatisticsEntity.fromPlayerStatistics(statistics)
            statisticsDao.insertOrUpdate(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetStatistics(): Result<Unit> {
        return try {
            statisticsDao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordGameResult(won: Boolean, score: Int): Result<Unit> {
        return try {
            val currentStats = getStatistics().getOrNull() ?: PlayerStatistics()
            val newStats = currentStats.copy(
                gamesPlayed = currentStats.gamesPlayed + 1,
                gamesWon = if (won) currentStats.gamesWon + 1 else currentStats.gamesWon,
                gamesLost = if (!won) currentStats.gamesLost + 1 else currentStats.gamesLost,
                totalScore = currentStats.totalScore + score,
                highestScore = maxOf(currentStats.highestScore, score),
                averageScore = (currentStats.totalScore.toFloat() + score) / (currentStats.gamesPlayed + 1),
                winRate = if (won) {
                    ((currentStats.gamesWon + 1).toFloat()) / (currentStats.gamesPlayed + 1)
                } else {
                    currentStats.gamesWon.toFloat() / (currentStats.gamesPlayed + 1)
                }
            )
            updateStatistics(newStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
