package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NotificationViewModel
    private lateinit var mockRepository: MockAchievementRepository

    @Before
    fun setup() {
        mockRepository = MockAchievementRepository()
        viewModel = NotificationViewModel(mockRepository)
    }

    @Test
    fun testCheckAndShowNotificationWithUnlockedAchievement() = runTest {
        val achievement = Achievement(
            id = "speed_demon",
            name = "Speed Demon",
            description = "Complete a game in under 30 seconds",
            unlockedAt = System.currentTimeMillis(),
            isUnlocked = true
        )
        mockRepository.achievementsFlow.emit(listOf(achievement))

        viewModel.checkAndShowNotification()

        val state = viewModel.notificationState.value
        assertTrue(state.isVisible)
        assertEquals("Speed Demon", state.achievement?.name)
    }

    @Test
    fun testDismissNotification() = runTest {
        viewModel.dismissNotification()
        val state = viewModel.notificationState.value
        assertFalse(state.isVisible)
    }

    private class MockAchievementRepository : AchievementRepository {
        val achievementsFlow = MutableStateFlow<List<Achievement>>(emptyList())

        override suspend fun unlockAchievement(achievementId: String) = Result.success(Unit)
        override suspend fun getUnlockedAchievements() =
            Result.success(achievementsFlow.value.filter { it.isUnlocked })
        override suspend fun getAllAchievements() = Result.success(achievementsFlow.value)
        override fun observeAchievements(): Flow<List<Achievement>> = achievementsFlow.asStateFlow()
        override suspend fun resetAchievements() = Result.success(Unit)
        override suspend fun syncAchievementCatalog() = Result.success(Unit)
        override suspend fun syncAchievements(achievements: List<Achievement>) = Result.success(Unit)
    }
}
