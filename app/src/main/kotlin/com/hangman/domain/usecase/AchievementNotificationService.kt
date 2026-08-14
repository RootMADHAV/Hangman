package com.hangman.domain.usecase

import com.hangman.domain.repository.AchievementRepository
import javax.inject.Inject

class AchievementNotificationService @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend fun getUnlockedAchievements(): List<String> {
        return achievementRepository.getUnlockedAchievements()
            .getOrNull()
            ?.map { "${it.name}: ${it.description}" }
            ?: emptyList()
    }

    suspend fun getMostRecentUnlock(): String? {
        return achievementRepository.getUnlockedAchievements()
            .getOrNull()
            ?.maxByOrNull { it.unlockedAt ?: 0L }
            ?.let { "${it.name}: ${it.description}" }
    }
}
