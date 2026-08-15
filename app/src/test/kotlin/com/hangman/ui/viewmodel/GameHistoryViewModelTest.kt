package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.repository.GameHistoryRepository
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
class GameHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: GameHistoryViewModel
    private lateinit var mockRepository: MockGameHistoryRepository

    @Before
    fun setup() {
        mockRepository = MockGameHistoryRepository()
        viewModel = GameHistoryViewModel(mockRepository)
    }

    @Test
    fun testGamesEmitFromRepository() = runTest {
        // stateIn(SharingStarted.Lazily) stays at its initial value until something
        // collects it, so start a collector before asserting.
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.games.collect { }
        }

        val games = listOf(
            GameHistoryEntry(
                word = "kotlin",
                difficulty = Difficulty.MEDIUM,
                won = true,
                score = 150,
                guessedLetters = setOf('K', 'O', 'T', 'L', 'I', 'N'),
                incorrectGuesses = setOf('A'),
                elapsedSeconds = 45
            )
        )
        mockRepository.gamesFlow.emit(games)

        val result = viewModel.games.value
        assertEquals(1, result.size)
        assertEquals("kotlin", result[0].word)

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
