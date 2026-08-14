package com.hangman.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameStatusTimerTest {

    @Test
    fun testGameStatusHasStartTime() {
        val word = Word("test", Difficulty.MEDIUM)
        val gameStatus = GameStatus(word = word)

        assertTrue(gameStatus.gameStartTime > 0)
    }

    @Test
    fun testGameStatusElapsedTimeCalculation() {
        val word = Word("test", Difficulty.MEDIUM)
        val startTime = System.currentTimeMillis()
        val gameStatus = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = startTime + 5000 // 5 seconds later
        )

        assertEquals(5, gameStatus.elapsedSeconds)
        assertEquals(5000, gameStatus.elapsedMillis)
    }

    @Test
    fun testGameStatusElapsedTimeWithoutEndTime() {
        val word = Word("test", Difficulty.MEDIUM)
        val startTime = System.currentTimeMillis() - 3000
        val gameStatus = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = null
        )

        val elapsedSeconds = gameStatus.elapsedSeconds
        assertTrue(elapsedSeconds >= 2 && elapsedSeconds <= 4)
    }

    @Test
    fun testGameStatusSpeedDemonThreshold() {
        val word = Word("test", Difficulty.EASY)
        val startTime = System.currentTimeMillis()

        // Fast game (20 seconds)
        val fastGame = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = startTime + 20000,
            state = GameState.WON
        )
        assertTrue(fastGame.elapsedSeconds <= 30)

        // Slow game (45 seconds)
        val slowGame = GameStatus(
            word = word,
            gameStartTime = startTime,
            gameEndTime = startTime + 45000,
            state = GameState.WON
        )
        assertFalse(slowGame.elapsedSeconds <= 30)
    }
}
