package com.LetterQuest.data.repository

import com.LetterQuest.domain.model.Difficulty
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WordRepositoryImplTest {

    private lateinit var repository: WordRepositoryImpl

    @Before
    fun setUp() {
        repository = WordRepositoryImpl()
    }

    @Test
    fun testGetRandomWordEasy() = runTest {
        val result = repository.getRandomWord(Difficulty.EASY)
        assertTrue(result.isSuccess)
        val word = result.getOrNull()
        assertNotNull(word)
        assertEquals(Difficulty.EASY, word?.difficulty)
    }

    @Test
    fun testGetRandomWordMedium() = runTest {
        val result = repository.getRandomWord(Difficulty.MEDIUM)
        assertTrue(result.isSuccess)
        val word = result.getOrNull()
        assertNotNull(word)
        assertEquals(Difficulty.MEDIUM, word?.difficulty)
    }

    @Test
    fun testGetRandomWordHard() = runTest {
        val result = repository.getRandomWord(Difficulty.HARD)
        assertTrue(result.isSuccess)
        val word = result.getOrNull()
        assertNotNull(word)
        assertEquals(Difficulty.HARD, word?.difficulty)
    }

    @Test
    fun testGetAllWords() = runTest {
        val result = repository.getAllWords()
        assertTrue(result.isSuccess)
        val words = result.getOrNull()
        assertNotNull(words)
        assertTrue(words?.size ?: 0 > 0)
    }

    @Test
    fun testGetWordsByDifficulty() = runTest {
        val result = repository.getWordsByDifficulty(Difficulty.EASY)
        assertTrue(result.isSuccess)
        val words = result.getOrNull()
        assertNotNull(words)
        assertTrue(words?.all { it.difficulty == Difficulty.EASY } ?: false)
    }

    @Test
    fun testGetWordsByDifficultyConsistency() = runTest {
        val easyWords = repository.getWordsByDifficulty(Difficulty.EASY).getOrNull() ?: emptyList()
        val mediumWords = repository.getWordsByDifficulty(Difficulty.MEDIUM).getOrNull() ?: emptyList()
        val hardWords = repository.getWordsByDifficulty(Difficulty.HARD).getOrNull() ?: emptyList()

        assertTrue(easyWords.isNotEmpty())
        assertTrue(mediumWords.isNotEmpty())
        assertTrue(hardWords.isNotEmpty())
    }

    @Test
    fun testRandomWordVariety() = runTest {
        val words = mutableSetOf<String>()
        repeat(20) {
            val result = repository.getRandomWord(Difficulty.MEDIUM)
            val word = result.getOrNull()
            if (word != null) {
                words.add(word.value)
            }
        }
        assertTrue(words.size > 1)
    }
}
