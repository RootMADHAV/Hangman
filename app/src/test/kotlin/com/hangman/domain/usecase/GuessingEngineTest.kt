package com.hangman.domain.usecase

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameState
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.GuessResult
import com.hangman.domain.model.Word
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GuessingEngineTest {

    private lateinit var gameStatus: GameStatus

    @Before
    fun setUp() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        gameStatus = GameStatus(word = word)
    }

    @Test
    fun testCorrectGuess() {
        val (newStatus, result) = GuessingEngine.processGuess(gameStatus, 'A')

        assertEquals(GuessResult.Correct, result)
        assertTrue('A' in newStatus.guessedLetters)
        assertEquals(gameStatus.remainingAttempts, newStatus.remainingAttempts)
        assertEquals(GameState.PLAYING, newStatus.state)
    }

    @Test
    fun testIncorrectGuess() {
        val (newStatus, result) = GuessingEngine.processGuess(gameStatus, 'Z')

        assertEquals(GuessResult.Incorrect, result)
        assertTrue('Z' in newStatus.incorrectGuesses)
        assertEquals(gameStatus.remainingAttempts - 1, newStatus.remainingAttempts)
        assertEquals(GameState.PLAYING, newStatus.state)
    }

    @Test
    fun testAlreadyGuessedLetter() {
        val statusAfterFirst = gameStatus.copy(guessedLetters = setOf('A'))
        val (newStatus, result) = GuessingEngine.processGuess(statusAfterFirst, 'A')

        assertEquals(GuessResult.AlreadyGuessed, result)
        assertEquals(statusAfterFirst, newStatus)
    }

    @Test
    fun testInvalidLetter() {
        val (newStatus, result) = GuessingEngine.processGuess(gameStatus, '1')

        assertEquals(GuessResult.Invalid, result)
        assertEquals(gameStatus, newStatus)
    }

    @Test
    fun testGameOverGuess() {
        val lostGame = gameStatus.copy(state = GameState.LOST)
        val (newStatus, result) = GuessingEngine.processGuess(lostGame, 'A')

        assertEquals(GuessResult.Invalid, result)
        assertEquals(lostGame, newStatus)
    }

    @Test
    fun testWinCondition() {
        var currentStatus = gameStatus
        val uniqueLetters = "HANGMAN".toSet()

        for (letter in uniqueLetters) {
            val (newStatus, result) = GuessingEngine.processGuess(currentStatus, letter)
            currentStatus = newStatus
            assertEquals(GuessResult.Correct, result)
        }

        assertEquals(GameState.WON, currentStatus.state)
        assertTrue(currentStatus.isWordComplete())
    }

    @Test
    fun testLossCondition() {
        var currentStatus = gameStatus
        val attempts = gameStatus.word.difficulty.maxAttempts

        for (i in 0 until attempts) {
            val letter = ('Z'.code - i).toChar()
            val (newStatus, _) = GuessingEngine.processGuess(currentStatus, letter)
            currentStatus = newStatus
        }

        assertEquals(GameState.LOST, currentStatus.state)
        assertEquals(0, currentStatus.remainingAttempts)
    }

    @Test
    fun testCaseInsensitivity() {
        val (statusLower, resultLower) = GuessingEngine.processGuess(gameStatus, 'a')
        val (statusUpper, resultUpper) = GuessingEngine.processGuess(gameStatus, 'A')

        assertEquals(GuessResult.Correct, resultLower)
        assertEquals(GuessResult.Correct, resultUpper)
        assertEquals(statusLower.guessedLetters, statusUpper.guessedLetters)
    }

    @Test
    fun testValidateLetter() {
        assertTrue(GuessingEngine.validateLetter('A'))
        assertTrue(GuessingEngine.validateLetter('z'))
        assertFalse(GuessingEngine.validateLetter('1'))
        assertFalse(GuessingEngine.validateLetter('!'))
        assertFalse(GuessingEngine.validateLetter(' '))
    }

    @Test
    fun testRevealedWord() {
        val word = "HANGMAN"
        var currentStatus = gameStatus

        for (i in 0..2) {
            val (newStatus, _) = GuessingEngine.processGuess(currentStatus, word[i])
            currentStatus = newStatus
        }

        val revealed = currentStatus.revealedWord
        assertEquals("HAN__AN", revealed)
    }
}
