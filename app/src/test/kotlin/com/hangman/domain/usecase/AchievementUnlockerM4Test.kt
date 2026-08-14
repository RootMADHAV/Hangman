package com.hangman.domain.usecase

import com.hangman.domain.model.Achievement
import com.hangman.domain.model.DailyChallenge
import com.hangman.domain.model.DailyStreak
import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameState
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.PlayerStatistics
import com.hangman.domain.model.UserTokens
import com.hangman.domain.model.Word
import com.hangman.domain.repository.AchievementRepository
import com.hangman.domain.repository.DailyChallengeRepository
import com.hangman.domain.repository.GameHistoryRepository
import com.hangman.domain.repository.StatisticsRepository
import com.hangman.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AchievementUnlockerM4Test {

    private lateinit var achievementUnlocker: AchievementUnlocker
    private lateinit var mockAchievementRepository: MockAchievementRepository
    private lateinit var mockGameHistoryRepository: MockGameHistoryRepository

    @Before
    fun setup() {
        mockAchievementRepository = MockAchievementRepository()
        mockGameHistoryRepository = MockGameHistoryRepository()

        achievementUnlocker = AchievementUnlocker(
            mockAchievementRepository,
            mockGameHistoryRepository,
            MockTokenRepository(),
            MockDailyChallengeRepository()
        )
    }

    private class MockTokenRepository : TokenRepository {
        override fun observeTokens(): Flow<UserTokens> = flowOf(UserTokens(0))
        override suspend fun getTokens() = Result.success(UserTokens(0))
        override suspend fun spendTokens(amount: Int) = Result.success(UserTokens(0))
        override suspend fun earnTokens(amount: Int) = Result.success(UserTokens(0))
    }

    private class MockDailyChallengeRepository : DailyChallengeRepository {
        override fun observeStreak(): Flow<DailyStreak> = flowOf(DailyStreak())
        override suspend fun getStreak() = Result.success(DailyStreak())
        override suspend fun getTodaysChallenge(): Result<DailyChallenge> =
            Result.failure(UnsupportedOperationException("Not used in this test"))
        override suspend fun recordCompletion(won: Boolean) = Result.success(DailyStreak())
    }

    @Test
    fun testFastSolveAchievementUnderThreshold() = runTest {
        val word = Word("test", Difficulty.EASY)
        val startTime = System.currentTimeMillis()
        val gameStatus = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = startTime + 20000, // 20 seconds
            state = GameState.WON,
            guessedLetters = setOf('T', 'E', 'S')
        )

        val statistics = PlayerStatistics()
        achievementUnlocker.evaluateAchievements(gameStatus, statistics)

        assertTrue(mockAchievementRepository.unlockedIds.contains("fast_solve"))
    }

    @Test
    fun testFastSolveAchievementOverThreshold() = runTest {
        val word = Word("test", Difficulty.EASY)
        val startTime = System.currentTimeMillis()
        val gameStatus = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = startTime + 40000, // 40 seconds
            state = GameState.WON,
            guessedLetters = setOf('T', 'E', 'S')
        )

        val statistics = PlayerStatistics()
        achievementUnlocker.evaluateAchievements(gameStatus, statistics)

        assertFalse(mockAchievementRepository.unlockedIds.contains("fast_solve"))
    }

    @Test
    fun testFiveStreakWithMaxStreak() = runTest {
        mockGameHistoryRepository.maxWinStreak = 5

        val word = Word("test", Difficulty.MEDIUM)
        val gameStatus = GameStatus(
            word = word,
            state = GameState.WON,
            guessedLetters = setOf('T', 'E', 'S')
        )

        val statistics = PlayerStatistics()
        achievementUnlocker.evaluateAchievements(gameStatus, statistics)

        assertTrue(mockAchievementRepository.unlockedIds.contains("five_streak"))
    }

    @Test
    fun testFiveStreakNotYetWithinMaxStreak() = runTest {
        mockGameHistoryRepository.maxWinStreak = 4

        val word = Word("test", Difficulty.MEDIUM)
        val gameStatus = GameStatus(
            word = word,
            state = GameState.WON,
            guessedLetters = setOf('T', 'E', 'S')
        )

        val statistics = PlayerStatistics()
        achievementUnlocker.evaluateAchievements(gameStatus, statistics)

        assertFalse(mockAchievementRepository.unlockedIds.contains("five_streak"))
    }

    private class MockAchievementRepository : AchievementRepository {
        val unlockedIds = mutableSetOf<String>()

        override suspend fun getAllAchievements(): Result<List<Achievement>> = Result.success(emptyList())
        override fun observeAchievements(): Flow<List<Achievement>> = emptyFlow()
        override suspend fun unlockAchievement(achievementId: String): Result<Unit> {
            unlockedIds.add(achievementId)
            return Result.success(Unit)
        }

        override suspend fun getUnlockedAchievements(): Result<List<Achievement>> = Result.success(emptyList())
        override suspend fun resetAchievements(): Result<Unit> = Result.success(Unit)
        override suspend fun syncAchievementCatalog(): Result<Unit> = Result.success(Unit)
    }

    private class MockGameHistoryRepository : GameHistoryRepository {
        var consecutiveWins = 0
        var maxWinStreak = 0

        override suspend fun addGameEntry(entry: com.hangman.domain.model.GameHistoryEntry): Result<Unit> =
            Result.success(Unit)

        override suspend fun getRecentGames(limit: Int): Result<List<com.hangman.domain.model.GameHistoryEntry>> =
            Result.success(emptyList())

        override suspend fun getAllGames(): Result<List<com.hangman.domain.model.GameHistoryEntry>> =
            Result.success(emptyList())

        override fun observeAllGames(): Flow<List<com.hangman.domain.model.GameHistoryEntry>> = emptyFlow()
        override suspend fun getWonGames(): Result<List<com.hangman.domain.model.GameHistoryEntry>> =
            Result.success(emptyList())

        override suspend fun getLostGames(): Result<List<com.hangman.domain.model.GameHistoryEntry>> =
            Result.success(emptyList())

        override suspend fun getConsecutiveWins(): Result<Int> = Result.success(consecutiveWins)
        override suspend fun getMaxWinStreak(): Result<Int> = Result.success(maxWinStreak)
        override suspend fun getGameCount(): Result<Int> = Result.success(0)
        override suspend fun deleteAll(): Result<Unit> = Result.success(Unit)
    }

    private class StatisticsRepositoryMock : StatisticsRepository {
        override fun observeStatistics(): Flow<PlayerStatistics> = emptyFlow()
        override suspend fun getStatistics(): Result<PlayerStatistics> =
            Result.success(PlayerStatistics())

        override suspend fun updateStatistics(statistics: PlayerStatistics): Result<Unit> =
            Result.success(Unit)

        override suspend fun recordGameResult(won: Boolean, score: Int): Result<Unit> =
            Result.success(Unit)

        override suspend fun resetStatistics(): Result<Unit> = Result.success(Unit)
    }
}
