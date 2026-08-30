package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface CloudSyncRepository {
    suspend fun uploadProfile(profile: PlayerProfile): Result<Unit>
    suspend fun downloadProfile(): Result<PlayerProfile?>
    suspend fun uploadGameHistory(entries: List<GameHistoryEntry>): Result<Unit>
    suspend fun downloadGameHistory(since: Long = 0L): Result<List<GameHistoryEntry>>
    suspend fun uploadAchievements(achievements: List<Achievement>): Result<Unit>
    suspend fun downloadAchievements(since: Long = 0L): Result<List<Achievement>>
    suspend fun uploadLeaderboardScore(
        metric: String,
        value: Float,
        gamesPlayed: Int,
        gamesWon: Int,
        username: String,
        nickname: String,
        avatarId: String
    ): Result<Unit>
    suspend fun deleteProfile(): Result<Unit>
    suspend fun deleteGameHistory(): Result<Unit>
    suspend fun deleteAchievements(): Result<Unit>
    fun observeSyncStatus(): Flow<SyncStatus>
    suspend fun getLastSyncTimestamp(): Long?
    suspend fun setLastSyncTimestamp(timestamp: Long)
}

data class SyncStatus(
    val isOnline: Boolean = false,
    val lastSyncAt: Long? = null,
    val pendingUploads: Int = 0,
    val lastError: String? = null
)
