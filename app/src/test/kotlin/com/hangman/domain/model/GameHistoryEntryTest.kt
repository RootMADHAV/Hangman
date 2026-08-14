package com.hangman.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameHistoryEntryTest {

    @Test
    fun testGameHistoryEntryCreation() {
        val entry = GameHistoryEntry(
            word = "kotlin",
            difficulty = Difficulty.MEDIUM,
            won = true,
            score = 150,
            guessedLetters = setOf('K', 'O', 'T', 'L', 'I', 'N'),
            incorrectGuesses = setOf('A', 'E'),
            elapsedSeconds = 45
        )

        assertEquals("kotlin", entry.word)
        assertEquals(Difficulty.MEDIUM, entry.difficulty)
        assertTrue(entry.won)
        assertEquals(150, entry.score)
        assertEquals(8, entry.totalGuesses)
        assertEquals(6, entry.correctGuesses)
    }

    @Test
    fun testGameHistoryEntryTotalGuesses() {
        val entry = GameHistoryEntry(
            word = "test",
            difficulty = Difficulty.EASY,
            won = true,
            score = 100,
            guessedLetters = setOf('T', 'E', 'S'),
            incorrectGuesses = setOf('A', 'B', 'C'),
            elapsedSeconds = 30
        )

        assertEquals(6, entry.totalGuesses)
    }

    @Test
    fun testGameHistoryEntryCorrectGuesses() {
        val entry = GameHistoryEntry(
            word = "game",
            difficulty = Difficulty.HARD,
            won = false,
            score = 50,
            guessedLetters = setOf('G', 'A', 'M', 'E'),
            incorrectGuesses = setOf('X', 'Y', 'Z'),
            elapsedSeconds = 120
        )

        assertEquals(4, entry.correctGuesses)
        assertEquals(7, entry.totalGuesses)
    }
}
