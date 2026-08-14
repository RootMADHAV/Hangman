package com.hangman.ui.viewmodel

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameHistoryEntry
import com.hangman.domain.repository.GameHistoryRepository
import com.hangman.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LeaderboardViewModel
    private lateinit var mockRepository: MockGameHistoryRepository

    @Before
    fun setup() {
        mockRepository = MockGameHistoryRepository()
        viewModel = LeaderboardViewModel(mockRepository)
    }

    @Test
    fun testLeaderboardSortsByScoreThenTime() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.leaderboard.collect { }
        }

        val games = listOf(
            GameHistoryEntry(word = "kotlin", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = emptySet(), incorrectGuesses = emptySet(), elapsedSeconds = 30),
            GameHistoryEntry(word = "java", difficulty = Difficulty.EASY, won = true, score = 150, guessedLetters = emptySet(), incorrectGuesses = emptySet(), elapsedSeconds = 20),
            GameHistoryEntry(word = "rust", difficulty = Difficulty.HARD, won = false, score = 50, guessedLetters = emptySet(), incorrectGuesses = emptySet(), elapsedSeconds = 60)
        )
        mockRepository.gamesFlow.emit(games)

        val result = viewModel.leaderboard.value
        assertEquals(2, result.size)
        assertEquals(150, result[0].score)
        assertEquals(100, result[1].score)

        collectJob.cancel()
    }

    @Test
    fun testLeaderboardFiltersLosses() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.leaderboard.collect { }
        }

        val games = listOf(
            GameHistoryEntry(word = "kotlin", difficulty = Difficulty.MEDIUM, won = true, score = 100, guessedLetters = emptySet(), incorrectGuesses = emptySet(), elapsedSeconds = 30),
            GameHistoryEntry(word = "java", difficulty = Difficulty.EASY, won = false, score = 150, guessedLetters = emptySet(), incorrectGuesses = emptySet(), elapsedSeconds = 20)
        )
        mockRepository.gamesFlow.emit(games)

        val result = viewModel.leaderboard.value
        assertEquals(1, result.size)
        assertTrue(result[0].won)

        collectJob.cancel()
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
