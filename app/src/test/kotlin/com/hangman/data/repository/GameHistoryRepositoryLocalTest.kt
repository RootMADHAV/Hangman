package com.LetterQuest.data.repository

import com.LetterQuest.data.local.dao.GameHistoryDao
import com.LetterQuest.data.local.entity.GameHistoryEntity
import com.LetterQuest.data.local.repository.GameHistoryRepositoryLocal
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameHistoryRepositoryLocalTest {

    private lateinit var repository: GameHistoryRepositoryLocal
    private lateinit var mockDao: MockGameHistoryDao

    @Before
    fun setup() {
        mockDao = MockGameHistoryDao()
        repository = GameHistoryRepositoryLocal(mockDao)
    }

    @Test
    fun testAddGameEntry() = runTest {
        val entry = GameHistoryEntry(
            word = "kotlin",
            difficulty = Difficulty.MEDIUM,
            won = true,
            score = 150,
            guessedLetters = setOf('K', 'O', 'T', 'L', 'I', 'N'),
            incorrectGuesses = setOf('A'),
            elapsedSeconds = 45
        )

        val result = repository.addGameEntry(entry)

        assertTrue(result.isSuccess)
        assertEquals(1, mockDao.insertedGames.size)
    }

    @Test
    fun testGetConsecutiveWins() = runTest {
        val won1 = GameHistoryEntity(
            uuid = "uuid1",
            word = "test",
            difficulty = "EASY",
            won = true,
            score = 100,
            guessedLetters = "T,E,S",
            incorrectGuesses = "",
            elapsedSeconds = 30,
            playedAt = System.currentTimeMillis()
        )
        val won2 = GameHistoryEntity(
            uuid = "uuid2",
            word = "kotlin",
            difficulty = "MEDIUM",
            won = true,
            score = 150,
            guessedLetters = "K,O,T,L,I,N",
            incorrectGuesses = "A",
            elapsedSeconds = 45,
            playedAt = System.currentTimeMillis() + 1000
        )
        val lost = GameHistoryEntity(
            uuid = "uuid3",
            word = "java",
            difficulty = "HARD",
            won = false,
            score = 50,
            guessedLetters = "J,A",
            incorrectGuesses = "X,Y,Z",
            elapsedSeconds = 120,
            playedAt = System.currentTimeMillis() + 2000
        )

        mockDao.games = listOf(won1, won2, lost)

        val result = repository.getConsecutiveWins()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun testGetMaxWinStreak() = runTest {
        val base = System.currentTimeMillis()
        val games = listOf(
            GameHistoryEntity(uuid = "a", word = "a", difficulty = "EASY", won = true,  score = 100, guessedLetters = "", incorrectGuesses = "", elapsedSeconds = 10, playedAt = base),
            GameHistoryEntity(uuid = "b", word = "b", difficulty = "EASY", won = true,  score = 100, guessedLetters = "", incorrectGuesses = "", elapsedSeconds = 10, playedAt = base + 1),
            GameHistoryEntity(uuid = "c", word = "c", difficulty = "EASY", won = true,  score = 100, guessedLetters = "", incorrectGuesses = "", elapsedSeconds = 10, playedAt = base + 2),
            GameHistoryEntity(uuid = "d", word = "d", difficulty = "EASY", won = false, score = 0,   guessedLetters = "", incorrectGuesses = "", elapsedSeconds = 10, playedAt = base + 3),
            GameHistoryEntity(uuid = "e", word = "e", difficulty = "EASY", won = true,  score = 100, guessedLetters = "", incorrectGuesses = "", elapsedSeconds = 10, playedAt = base + 4)
        )
        mockDao.games = games.sortedByDescending { it.playedAt }

        val result = repository.getMaxWinStreak()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
    }

    @Test
    fun testGetGameCount() = runTest {
        val game1 = GameHistoryEntity(
            uuid = "uuid1",
            word = "test",
            difficulty = "EASY",
            won = true,
            score = 100,
            guessedLetters = "T,E,S",
            incorrectGuesses = "",
            elapsedSeconds = 30
        )
        val game2 = GameHistoryEntity(
            uuid = "uuid2",
            word = "kotlin",
            difficulty = "MEDIUM",
            won = false,
            score = 50,
            guessedLetters = "K,O",
            incorrectGuesses = "X,Y",
            elapsedSeconds = 60
        )

        mockDao.games = listOf(game1, game2)
        mockDao.gameCount = 2

        val result = repository.getGameCount()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    private class MockTokenRepository : com.LetterQuest.domain.repository.TokenRepository {
        override fun observeTokens(): Flow<com.LetterQuest.domain.model.UserTokens> =
            emptyFlow()
        override suspend fun getTokens() = Result.success(com.LetterQuest.domain.model.UserTokens(0))
        override suspend fun spendTokens(amount: Int) = Result.success(com.LetterQuest.domain.model.UserTokens(0))
        override suspend fun earnTokens(amount: Int) = Result.success(com.LetterQuest.domain.model.UserTokens(0))
        override suspend fun reset() {}
    }

    private class MockGameHistoryDao : GameHistoryDao {
        val insertedGames = mutableListOf<GameHistoryEntity>()
        var games = listOf<GameHistoryEntity>()
        var gameCount = 0

        override suspend fun insertGame(game: GameHistoryEntity) {
            insertedGames.add(game)
        }

        override suspend fun getRecentGames(limit: Int): List<GameHistoryEntity> =
            games.take(limit)

        override suspend fun getAllGames(): List<GameHistoryEntity> = games
        override fun observeAllGames(): Flow<List<GameHistoryEntity>> = emptyFlow()
        override suspend fun getWonGames(): List<GameHistoryEntity> =
            games.filter { it.won }

        override suspend fun getLostGames(): List<GameHistoryEntity> =
            games.filter { !it.won }

        override suspend fun getConsecutiveWins(fromTime: Long): Int {
            var count = 0
            for (game in games.sortedByDescending { it.playedAt }) {
                if (game.won) count++ else break
            }
            return count
        }

        override suspend fun deleteAll() {
            games = emptyList()
        }

        override suspend fun getGameCount(): Int = gameCount

        override suspend fun getByUuid(uuid: String): GameHistoryEntity? =
            games.find { it.uuid == uuid }

        override suspend fun updateScore(uuid: String, score: Int, sessionScore: Int, updatedAt: Long) {
            games = games.map {
                if (it.uuid == uuid) it.copy(score = score, sessionScore = sessionScore, updatedAt = updatedAt) else it
            }
        }

        override suspend fun upsertByUuid(uuid: String, word: String, difficulty: String, won: Boolean, score: Int, sessionScore: Int, guessedLetters: String, incorrectGuesses: String, elapsedSeconds: Long, playedAt: Long, updatedAt: Long, category: String?, hintsUsed: Int, gameMode: String) {
            val existing = games.find { it.uuid == uuid }
            games = if (existing != null) {
                games.map { if (it.uuid == uuid) it.copy(score = score, sessionScore = sessionScore, updatedAt = updatedAt) else it }
            } else {
                games + GameHistoryEntity(uuid = uuid, word = word, difficulty = difficulty, won = won, score = score, sessionScore = sessionScore, guessedLetters = guessedLetters, incorrectGuesses = incorrectGuesses, elapsedSeconds = elapsedSeconds, playedAt = playedAt, updatedAt = updatedAt, category = category, hintsUsed = hintsUsed, gameMode = gameMode)
            }
        }
    }
}
