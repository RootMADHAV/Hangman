package com.hangman.domain.repository

import com.hangman.domain.model.GameHistoryEntry
import kotlinx.coroutines.flow.Flow

interface GameHistoryRepository {
    suspend fun addGameEntry(entry: GameHistoryEntry): Result<Unit>
    suspend fun getRecentGames(limit: Int = 20): Result<List<GameHistoryEntry>>
    suspend fun getAllGames(): Result<List<GameHistoryEntry>>
    fun observeAllGames(): Flow<List<GameHistoryEntry>>
    suspend fun getWonGames(): Result<List<GameHistoryEntry>>
    suspend fun getLostGames(): Result<List<GameHistoryEntry>>
    suspend fun getConsecutiveWins(): Result<Int>
    /** Best-ever win streak across all recorded games (never resets on a loss). */
    suspend fun getMaxWinStreak(): Result<Int>
    suspend fun getGameCount(): Result<Int>
    suspend fun deleteAll(): Result<Unit>
}
