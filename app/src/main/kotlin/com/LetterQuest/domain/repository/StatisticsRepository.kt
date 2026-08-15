package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.PlayerStatistics
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun getStatistics(): Result<PlayerStatistics>
    fun observeStatistics(): Flow<PlayerStatistics>
    suspend fun updateStatistics(statistics: PlayerStatistics): Result<Unit>
    suspend fun resetStatistics(): Result<Unit>
    suspend fun recordGameResult(won: Boolean, score: Int): Result<Unit>
}
