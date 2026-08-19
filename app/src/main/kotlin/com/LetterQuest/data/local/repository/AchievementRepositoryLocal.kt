package com.LetterQuest.data.local.repository

import com.LetterQuest.data.local.dao.AchievementDao
import com.LetterQuest.data.local.entity.AchievementEntity
import com.LetterQuest.data.local.entity.toEntity
import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.AchievementCatalog
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class AchievementRepositoryLocal @Inject constructor(
    private val achievementDao: AchievementDao,
    private val tokenRepository: TokenRepository
) : AchievementRepository {

    private val unlockMutex = Mutex()

    override suspend fun syncAchievementCatalog(): Result<Unit> =
        try {
            for (milestone in AchievementCatalog.achievements) {
                val existing = achievementDao.getAchievementById(milestone.id)
                if (existing == null) {
                    achievementDao.insertOrUpdate(milestone.toEntity())
                } else if (existing.name != milestone.name ||
                    existing.description != milestone.description ||
                    existing.icon != milestone.icon
                ) {
                    // Definition changed: update it while preserving the unlock state.
                    achievementDao.insertOrUpdate(
                        milestone.toEntity(
                            isUnlocked = existing.isUnlocked,
                            unlockedAt = existing.unlockedAt
                        )
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun observeAchievements(): Flow<List<Achievement>> =
        achievementDao.observeAchievements().map { entities ->
            merge(entities)
        }

    override suspend fun getAllAchievements(): Result<List<Achievement>> =
        try {
            Result.success(merge(achievementDao.getAllAchievements()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getUnlockedAchievements(): Result<List<Achievement>> =
        try {
            Result.success(merge(achievementDao.getUnlockedAchievements()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun unlockAchievement(achievementId: String): Result<Unit> =
        try {
            val milestone = AchievementCatalog.getAchievementById(achievementId)
                ?: return Result.failure(IllegalArgumentException("Unknown achievement: $achievementId"))
            unlockMutex.withLock {
                val existing = achievementDao.getAchievementById(achievementId)

                val isFirstUnlock = existing?.isUnlocked != true
                achievementDao.insertOrUpdate(
                    milestone.toEntity(
                        isUnlocked = true,
                        unlockedAt = existing?.unlockedAt ?: System.currentTimeMillis()
                    )
                )
                if (isFirstUnlock && milestone.rewardTokens > 0) {
                    tokenRepository.earnTokens(milestone.rewardTokens)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun resetAchievements(): Result<Unit> =
        try {
            achievementDao.deleteAll()
            syncAchievementCatalog()
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun syncAchievements(achievements: List<Achievement>): Result<Unit> =
        try {
            achievementDao.deleteAll()
            for (achievement in achievements) {
                achievementDao.insertOrUpdate(AchievementEntity.fromAchievement(achievement))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Merges persisted unlock state onto the catalog so the UI always shows the
     * full achievement set once it has been seeded via [syncAchievementCatalog].
     */
    private fun merge(
        entities: List<com.LetterQuest.data.local.entity.AchievementEntity>
    ): List<Achievement> {
        val byId = entities.associateBy { it.id }
        val fullySeeded = AchievementCatalog.achievements.all { byId.containsKey(it.id) }
        return if (fullySeeded) {
            AchievementCatalog.achievements.map { milestone ->
                val e = byId.getValue(milestone.id)
                Achievement(
                    id = milestone.id,
                    name = milestone.name,
                    description = milestone.description,
                    icon = milestone.icon,
                    condition = milestone.condition,
                    rewardTokens = milestone.rewardTokens,
                    unlockedAt = e.unlockedAt,
                    isUnlocked = e.isUnlocked
                )
            }
        } else {
            entities.map { it.toAchievement() }
        }
    }
}
