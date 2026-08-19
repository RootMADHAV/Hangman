package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.PlayerProfile
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.repository.CloudSyncRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.SyncStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import javax.inject.Inject

class CloudSyncUseCase @Inject constructor(
    private val cloudSyncRepository: CloudSyncRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val authRepository: AuthRepository,
    private val syncQueue: com.LetterQuest.data.local.repository.SyncQueue
) {
    suspend fun syncAll(): Result<SyncResult> = try {
        syncWithRetry(maxRetries = 3)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun syncWithRetry(maxRetries: Int = 3): Result<SyncResult> {
        var lastError: Exception? = null
        for (attempt in 0 until maxRetries) {
            try {
                return performSync()
            } catch (e: Exception) {
                lastError = e
                if (isRetryable(e) && attempt < maxRetries - 1) {
                    val delayMs = (1000L * (2.0.pow(attempt.toDouble()))).toLong()
                    kotlinx.coroutines.delay(delayMs)
                } else {
                    throw e
                }
            }
        }
        return Result.failure(lastError ?: Exception("Sync failed after retries"))
    }

    private suspend fun performSync(): Result<SyncResult> {
        val localProfile: PlayerProfile? = authRepository.profile.first()
        if (localProfile != null) {
            cloudSyncRepository.uploadProfile(localProfile)
        }

        val remoteProfile: PlayerProfile? = cloudSyncRepository.downloadProfile().getOrNull()
        if (remoteProfile != null) {
            val existing: PlayerProfile? = authRepository.profile.first()
            if (existing == null || remoteProfile.updatedAt > existing.updatedAt) {
                authRepository.importProfile(remoteProfile)
            }
        }

        val localGames = gameHistoryRepository.getAllGames().getOrNull().orEmpty()
        val remoteGames = cloudSyncRepository.downloadGameHistory().getOrNull().orEmpty()
        val mergedGames = mergeGameHistory(localGames, remoteGames)

        val localAchievements = achievementRepository.getAllAchievements().getOrNull().orEmpty()
        val remoteAchievements = cloudSyncRepository.downloadAchievements().getOrNull().orEmpty()
        val mergedAchievements = mergeAchievements(localAchievements, remoteAchievements)

        gameHistoryRepository.syncGames(mergedGames)
        achievementRepository.syncAchievements(mergedAchievements)

        cloudSyncRepository.uploadGameHistory(mergedGames)
        cloudSyncRepository.uploadAchievements(mergedAchievements)

        syncQueue.drain()

        return Result.success(
            SyncResult(
                syncedGames = mergedGames.size,
                syncedAchievements = mergedAchievements.size
            )
        )
    }

    private fun isRetryable(e: Exception): Boolean {
        return e is java.io.IOException ||
                e is kotlinx.coroutines.TimeoutCancellationException ||
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("unavailable", ignoreCase = true) == true
    }

    fun observeSyncStatus(): Flow<SyncStatus> = cloudSyncRepository.observeSyncStatus()

    private fun mergeGameHistory(local: List<GameHistoryEntry>, remote: List<GameHistoryEntry>): List<GameHistoryEntry> {
        val byUuid = mutableMapOf<String, GameHistoryEntry>()
        for (entry in local) {
            if (entry.uuid.isNotBlank()) {
                byUuid[entry.uuid] = entry
            }
        }
        for (entry in remote) {
            if (entry.uuid.isBlank()) continue
            val existing = byUuid[entry.uuid]
            if (existing == null || entry.updatedAt > existing.updatedAt) {
                byUuid[entry.uuid] = entry
            }
        }
        return byUuid.values.toList()
    }

    private fun mergeAchievements(local: List<Achievement>, remote: List<Achievement>): List<Achievement> {
        val byId = mutableMapOf<String, Achievement>()
        for (achievement in local) {
            byId[achievement.id] = achievement
        }
        for (achievement in remote) {
            val existing = byId[achievement.id]
            if (existing == null) {
                byId[achievement.id] = achievement
            } else if (achievement.isUnlocked && !existing.isUnlocked) {
                byId[achievement.id] = achievement
            } else if (achievement.isUnlocked && existing.isUnlocked && achievement.unlockedAt != null && existing.unlockedAt != null && achievement.unlockedAt > existing.unlockedAt) {
                byId[achievement.id] = achievement
            }
        }
        return byId.values.toList()
    }
}

data class SyncResult(
    val syncedGames: Int = 0,
    val syncedAchievements: Int = 0
)
