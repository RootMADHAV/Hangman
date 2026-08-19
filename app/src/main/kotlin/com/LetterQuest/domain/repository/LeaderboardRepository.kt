package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.GlobalLeaderboardEntry
import com.LetterQuest.domain.model.LeaderboardMetric
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun observeLeaderboard(metric: LeaderboardMetric, limit: Int = 50): Flow<List<GlobalLeaderboardEntry>>
    fun observeCurrentUserEntry(metric: LeaderboardMetric, userId: String): Flow<GlobalLeaderboardEntry?>
    suspend fun getCurrentUserRank(metric: LeaderboardMetric, userId: String): Result<GlobalLeaderboardEntry?>
    suspend fun submitScore(
        metric: LeaderboardMetric,
        value: Float,
        gamesPlayed: Int,
        gamesWon: Int,
        username: String = "",
        nickname: String = "Player",
        avatarId: String = "avatar_1"
    ): Result<Unit>
}
