package com.LetterQuest.data.local.repository

import com.LetterQuest.data.local.dao.StatisticsDao
import com.LetterQuest.data.local.entity.StatisticsEntity
import com.LetterQuest.domain.model.PlayerStatistics
import com.LetterQuest.domain.repository.StatisticsRepository
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
            val wonInt = if (won) 1 else 0
            val lostInt = if (won) 0 else 1
            statisticsDao.atomicUpdate(won = wonInt, lost = lostInt, score = score)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
