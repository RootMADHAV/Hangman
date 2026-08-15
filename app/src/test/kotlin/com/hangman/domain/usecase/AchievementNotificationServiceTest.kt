package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AchievementNotificationServiceTest {

    private lateinit var service: AchievementNotificationService
    private lateinit var mockRepository: MockAchievementRepository

    @Before
    fun setup() {
        mockRepository = MockAchievementRepository()
        service = AchievementNotificationService(mockRepository)
    }

    @Test
    fun testGetUnlockedAchievements() = runTest {
        val achievements = listOf(
            Achievement("first_win", "First Victory", "Win your first game"),
            Achievement("perfect_game", "Perfect", "Win without wrong guesses", isUnlocked = true, unlockedAt = 100)
        )
        mockRepository.achievements = achievements.filter { it.isUnlocked }

        val result = service.getUnlockedAchievements()

        assertEquals(1, result.size)
        assertTrue(result[0].contains("Perfect"))
    }

    @Test
    fun testGetMostRecentUnlock() = runTest {
        val achievements = listOf(
            Achievement("first_win", "First Victory", "Win your first game", isUnlocked = true, unlockedAt = 100),
            Achievement("perfect_game", "Perfect", "Win without wrong guesses", isUnlocked = true, unlockedAt = 200)
        )
        mockRepository.achievements = achievements

        val result = service.getMostRecentUnlock()

        assertNotNull(result)
        assertTrue(result!!.contains("Perfect"))
    }

    @Test
    fun testGetMostRecentUnlockWhenNone() = runTest {
        mockRepository.achievements = emptyList()

        val result = service.getMostRecentUnlock()

        assertNull(result)
    }

    private class MockAchievementRepository : AchievementRepository {
        var achievements = listOf<Achievement>()

        override suspend fun getAllAchievements(): Result<List<Achievement>> =
            Result.success(achievements)

        override fun observeAchievements(): Flow<List<Achievement>> = emptyFlow()

        override suspend fun unlockAchievement(achievementId: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun getUnlockedAchievements(): Result<List<Achievement>> =
            Result.success(achievements.filter { it.isUnlocked })

        override suspend fun resetAchievements(): Result<Unit> = Result.success(Unit)
        override suspend fun syncAchievementCatalog(): Result<Unit> = Result.success(Unit)
    }
}
