package com.hangman.domain.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameStatusTest {

    private lateinit var gameStatus: GameStatus

    @Before
    fun setUp() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        gameStatus = GameStatus(word = word)
    }

    @Test
    fun testRevealedWordInitial() {
        assertEquals("_______", gameStatus.revealedWord)
    }

    @Test
    fun testRevealedWordPartial() {
        val status = gameStatus.copy(guessedLetters = setOf('H', 'A', 'N'))
        assertEquals("HAN__AN", status.revealedWord)
    }

    @Test
    fun testRevealedWordComplete() {
        val status = gameStatus.copy(
            guessedLetters = setOf('H', 'A', 'N', 'G', 'M')
        )
        assertEquals("HANGMAN", status.revealedWord)
    }

    @Test
    fun testIsGameOverWon() {
        val wonStatus = gameStatus.copy(state = GameState.WON)
        assertTrue(wonStatus.isGameOver)
    }

    @Test
    fun testIsGameOverLost() {
        val lostStatus = gameStatus.copy(state = GameState.LOST)
        assertTrue(lostStatus.isGameOver)
    }

    @Test
    fun testIsGameOverPlaying() {
        assertFalse(gameStatus.isGameOver)
    }

    @Test
    fun testIsLetterGuessed() {
        val status = gameStatus.copy(guessedLetters = setOf('A', 'H'))
        assertTrue(status.isLetterGuessed('A'))
        assertTrue(status.isLetterGuessed('h'))
        assertFalse(status.isLetterGuessed('Z'))
    }

    @Test
    fun testIsLetterGuessedIncorrect() {
        val status = gameStatus.copy(incorrectGuesses = setOf('Z', 'Q'))
        assertTrue(status.isLetterGuessed('Z'))
        assertTrue(status.isLetterGuessed('z'))
        assertFalse(status.isLetterGuessed('A'))
    }

    @Test
    fun testTotalGuesses() {
        val status = gameStatus.copy(
            guessedLetters = setOf('A', 'H', 'N'),
            incorrectGuesses = setOf('Z', 'Q')
        )
        assertEquals(5, status.totalGuesses)
    }

    @Test
    fun testCorrectGuesses() {
        val status = gameStatus.copy(guessedLetters = setOf('H', 'A', 'N', 'Z'))
        val correctGuesses = status.correctGuesses
        assertEquals(3, correctGuesses.size)
        assertTrue('H' in correctGuesses)
        assertTrue('A' in correctGuesses)
        assertTrue('N' in correctGuesses)
        assertFalse('Z' in correctGuesses)
    }

    @Test
    fun testIsWordComplete() {
        val incomplete = gameStatus.copy(guessedLetters = setOf('H', 'A', 'N'))
        assertFalse(incomplete.isWordComplete())

        val complete = gameStatus.copy(guessedLetters = setOf('H', 'A', 'N', 'G', 'M'))
        assertTrue(complete.isWordComplete())
    }

    @Test
    fun testWordNormalization() {
        val status = gameStatus.copy(guessedLetters = setOf('H', 'A', 'N'))
        assertTrue(status.isLetterGuessed('H'))
        assertTrue(status.isLetterGuessed('h'))
        assertTrue(status.isLetterGuessed('a'))
    }
}
