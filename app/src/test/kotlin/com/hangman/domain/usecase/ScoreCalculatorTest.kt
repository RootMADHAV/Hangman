package com.hangman.domain.usecase

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameState
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.Word
import org.junit.Assert.*
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun testScoreCalculationForWon() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        val gameStatus = GameStatus(
            word = word,
            state = GameState.WON,
            remainingAttempts = 5
        )

        val score = ScoreCalculator.calculateScore(gameStatus)
        assertTrue(score > 0)
    }

    @Test
    fun testScoreZeroForLost() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        val gameStatus = GameStatus(
            word = word,
            state = GameState.LOST,
            remainingAttempts = 0
        )

        val score = ScoreCalculator.calculateScore(gameStatus)
        assertEquals(0, score)
    }

    @Test
    fun testScoreZeroForPlaying() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        val gameStatus = GameStatus(
            word = word,
            state = GameState.PLAYING,
            remainingAttempts = 5
        )

        val score = ScoreCalculator.calculateScore(gameStatus)
        assertEquals(0, score)
    }

    @Test
    fun testLongerWordHigherScore() {
        val shortWord = Word("CAT", Difficulty.EASY)
        val longWord = Word("ALGORITHM", Difficulty.HARD)

        val shortGameStatus = GameStatus(
            word = shortWord,
            state = GameState.WON,
            remainingAttempts = 5
        )
        val longGameStatus = GameStatus(
            word = longWord,
            state = GameState.WON,
            remainingAttempts = 5
        )

        val shortScore = ScoreCalculator.calculateScore(shortGameStatus)
        val longScore = ScoreCalculator.calculateScore(longGameStatus)

        assertTrue(longScore > shortScore)
    }

    @Test
    fun testMoreRemainingAttemptsHigherScore() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)

        val statusWithMoreAttempts = GameStatus(
            word = word,
            state = GameState.WON,
            remainingAttempts = 7
        )
        val statusWithFewerAttempts = GameStatus(
            word = word,
            state = GameState.WON,
            remainingAttempts = 2
        )

        val scoreMore = ScoreCalculator.calculateScore(statusWithMoreAttempts)
        val scoreFewer = ScoreCalculator.calculateScore(statusWithFewerAttempts)

        assertTrue(scoreMore > scoreFewer)
    }

    @Test
    fun testDifficultyMultiplier() {
        val easyWord = Word("CAT", Difficulty.EASY)
        val mediumWord = Word("HANGMAN", Difficulty.MEDIUM)
        val hardWord = Word("ALGORITHM", Difficulty.HARD)

        val easyStatus = GameStatus(word = easyWord, state = GameState.WON, remainingAttempts = 8)
        val mediumStatus = GameStatus(word = mediumWord, state = GameState.WON, remainingAttempts = 8)
        val hardStatus = GameStatus(word = hardWord, state = GameState.WON, remainingAttempts = 8)

        val easyScore = ScoreCalculator.calculateScore(easyStatus)
        val mediumScore = ScoreCalculator.calculateScore(mediumStatus)
        val hardScore = ScoreCalculator.calculateScore(hardStatus)

        assertTrue(mediumScore > easyScore)
        assertTrue(hardScore > mediumScore)
    }
}
