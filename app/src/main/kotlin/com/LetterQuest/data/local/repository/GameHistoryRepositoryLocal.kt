package com.LetterQuest.data.local.repository

import com.LetterQuest.data.local.dao.GameHistoryDao
import com.LetterQuest.data.local.entity.GameHistoryEntity
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.repository.GameHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameHistoryRepositoryLocal @Inject constructor(
    private val gameHistoryDao: GameHistoryDao
) : GameHistoryRepository {

    override suspend fun addGameEntry(entry: GameHistoryEntry): Result<Unit> {
        return try {
            gameHistoryDao.insertGame(GameHistoryEntity.fromGameHistoryEntry(entry))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentGames(limit: Int): Result<List<GameHistoryEntry>> {
        return try {
            val games = gameHistoryDao.getRecentGames(limit)
            Result.success(games.map { it.toGameHistoryEntry() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllGames(): Result<List<GameHistoryEntry>> {
        return try {
            val games = gameHistoryDao.getAllGames()
            Result.success(games.map { it.toGameHistoryEntry() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAllGames(): Flow<List<GameHistoryEntry>> {
        return gameHistoryDao.observeAllGames()
            .map { games -> games.map { it.toGameHistoryEntry() } }
    }

    override suspend fun getWonGames(): Result<List<GameHistoryEntry>> {
        return try {
            val games = gameHistoryDao.getWonGames()
            Result.success(games.map { it.toGameHistoryEntry() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLostGames(): Result<List<GameHistoryEntry>> {
        return try {
            val games = gameHistoryDao.getLostGames()
            Result.success(games.map { it.toGameHistoryEntry() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConsecutiveWins(): Result<Int> {
        return try {
            val games = gameHistoryDao.getAllGames()
            var consecutiveWins = 0
            for (game in games) {
                if (game.won) {
                    consecutiveWins++
                } else {
                    break
                }
            }
            Result.success(consecutiveWins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMaxWinStreak(): Result<Int> {
        return try {
            // Ascending by playedAt so we walk the timeline in order.
            val games = gameHistoryDao.getAllGames().sortedBy { it.playedAt }
            var current = 0
            var best = 0
            for (game in games) {
                if (game.won) {
                    current++
                    if (current > best) best = current
                } else {
                    current = 0
                }
            }
            Result.success(best)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGameCount(): Result<Int> {
        return try {
            val count = gameHistoryDao.getGameCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAll(): Result<Unit> {
        return try {
            gameHistoryDao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
