package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.PlayerStatistics
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mockStatisticsRepository: MockStatisticsRepository
    private lateinit var mockAchievementRepository: MockAchievementRepository
    private lateinit var mockGameHistoryRepository: MockGameHistoryRepository

    @Before
    fun setup() {
        mockStatisticsRepository = MockStatisticsRepository()
        mockAchievementRepository = MockAchievementRepository()
        mockGameHistoryRepository = MockGameHistoryRepository()
        viewModel = ProfileViewModel(
            mockStatisticsRepository,
            mockAchievementRepository,
            mockGameHistoryRepository
        )
    }

    @Test
    fun testProfileStateWithGameHistory() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profileState.collect { }
        }

        val games = listOf(
            GameHistoryEntry(word = "kotlin", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = setOf('K', 'O'), incorrectGuesses = setOf(), elapsedSeconds = 30),
            GameHistoryEntry(word = "java", difficulty = Difficulty.EASY, won = true, score = 150, guessedLetters = setOf('J', 'A'), incorrectGuesses = setOf(), elapsedSeconds = 20),
            GameHistoryEntry(word = "rust", difficulty = Difficulty.HARD, won = false, score = 50, guessedLetters = setOf('R'), incorrectGuesses = setOf('X'), elapsedSeconds = 60)
        )
        mockGameHistoryRepository.gamesFlow.emit(games)

        val state = viewModel.profileState.value
        assertEquals(3, state.totalGames)
        assertEquals(66.67, state.winPercentage, 0.01)

        collectJob.cancel()
    }

    @Test
    fun testProfileStateWithAchievements() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profileState.collect { }
        }

        val achievements = listOf(
            Achievement(id = "first_victory", name = "First Victory", description = "Win your first game", unlockedAt = System.currentTimeMillis(), isUnlocked = true)
        )
        mockAchievementRepository.achievementsFlow.emit(achievements)

        val state = viewModel.profileState.value
        assertEquals(1, state.achievements.size)
        assertEquals("First Victory", state.achievements[0].name)

        collectJob.cancel()
    }

    @Test
    fun testWinPercentageCalculation() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profileState.collect { }
        }

        val games = listOf(
            GameHistoryEntry(word = "word1", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = setOf(), incorrectGuesses = setOf(), elapsedSeconds = 30),
            GameHistoryEntry(word = "word2", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = setOf(), incorrectGuesses = setOf(), elapsedSeconds = 30),
            GameHistoryEntry(word = "word3", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = setOf(), incorrectGuesses = setOf(), elapsedSeconds = 30),
            GameHistoryEntry(word = "word4", difficulty = Difficulty.MEDIUM, won = false, score = 50, guessedLetters = setOf(), incorrectGuesses = setOf(), elapsedSeconds = 60)
        )
        mockGameHistoryRepository.gamesFlow.emit(games)

        val state = viewModel.profileState.value
        assertEquals(75.0, state.winPercentage, 0.01)

        collectJob.cancel()
    }

    private class MockStatisticsRepository : StatisticsRepository {
        override suspend fun getStatistics() = Result.success(PlayerStatistics())
        override fun observeStatistics() = MutableStateFlow(PlayerStatistics()).asStateFlow()
        override suspend fun updateStatistics(statistics: PlayerStatistics) = Result.success(Unit)
        override suspend fun resetStatistics() = Result.success(Unit)
        override suspend fun recordGameResult(won: Boolean, score: Int) = Result.success(Unit)
    }

    private class MockAchievementRepository : AchievementRepository {
        val achievementsFlow = MutableStateFlow<List<Achievement>>(emptyList())
        override suspend fun unlockAchievement(achievementId: String) = Result.success(Unit)
        override suspend fun getUnlockedAchievements() = Result.success(emptyList<Achievement>())
        override suspend fun getAllAchievements() = Result.success(emptyList<Achievement>())
        override fun observeAchievements(): Flow<List<Achievement>> = achievementsFlow.asStateFlow()
        override suspend fun resetAchievements() = Result.success(Unit)
        override suspend fun syncAchievementCatalog() = Result.success(Unit)
    }

    private class MockGameHistoryRepository : GameHistoryRepository {
        val gamesFlow = MutableStateFlow<List<GameHistoryEntry>>(emptyList())
        override suspend fun addGameEntry(entry: GameHistoryEntry) = Result.success(Unit)
        override suspend fun getRecentGames(limit: Int) = Result.success(emptyList<GameHistoryEntry>())
        override suspend fun getAllGames() = Result.success(emptyList<GameHistoryEntry>())
        override fun observeAllGames(): Flow<List<GameHistoryEntry>> = gamesFlow.asStateFlow()
        override suspend fun getWonGames() = Result.success(emptyList<GameHistoryEntry>())
        override suspend fun getLostGames() = Result.success(emptyList<GameHistoryEntry>())
        override suspend fun getConsecutiveWins() = Result.success(0)
        override suspend fun getMaxWinStreak() = Result.success(0)
        override suspend fun getGameCount() = Result.success(0)
        override suspend fun deleteAll() = Result.success(Unit)
    }
}
