package com.hangman.domain.repository

import com.hangman.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun observeAchievements(): Flow<List<Achievement>>
    suspend fun getAllAchievements(): Result<List<Achievement>>
    suspend fun getUnlockedAchievements(): Result<List<Achievement>>
    suspend fun unlockAchievement(achievementId: String): Result<Unit>
    suspend fun resetAchievements(): Result<Unit>
    suspend fun syncAchievementCatalog(): Result<Unit>
}
