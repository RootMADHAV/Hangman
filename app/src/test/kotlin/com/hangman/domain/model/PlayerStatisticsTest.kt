package com.hangman.domain.model

import org.junit.Assert.*
import org.junit.Test

class PlayerStatisticsTest {

    @Test
    fun testDefaultStatistics() {
        val stats = PlayerStatistics()
        assertEquals(0, stats.gamesPlayed)
        assertEquals(0, stats.gamesWon)
        assertEquals(0, stats.gamesLost)
        assertEquals(0, stats.totalScore)
        assertEquals(0, stats.highestScore)
    }

    @Test
    fun testWinPercentageZeroGames() {
        val stats = PlayerStatistics()
        assertEquals(0f, stats.winPercentage)
    }

    @Test
    fun testWinPercentage50() {
        val stats = PlayerStatistics(
            gamesPlayed = 10,
            gamesWon = 5,
            gamesLost = 5
        )
        assertEquals(50f, stats.winPercentage)
    }

    @Test
    fun testWinPercentage100() {
        val stats = PlayerStatistics(
            gamesPlayed = 5,
            gamesWon = 5,
            gamesLost = 0
        )
        assertEquals(100f, stats.winPercentage)
    }

    @Test
    fun testWinPercentage0() {
        val stats = PlayerStatistics(
            gamesPlayed = 5,
            gamesWon = 0,
            gamesLost = 5
        )
        assertEquals(0f, stats.winPercentage)
    }

    @Test
    fun testAverageScoreCalculated() {
        val stats = PlayerStatistics(
            gamesPlayed = 5,
            totalScore = 500
        )
        assertEquals(100f, stats.averageScoreCalculated)
    }

    @Test
    fun testAverageScoreZeroGames() {
        val stats = PlayerStatistics()
        assertEquals(0f, stats.averageScoreCalculated)
    }

    @Test
    fun testHighestScore() {
        val stats = PlayerStatistics(
            gamesPlayed = 3,
            totalScore = 300,
            highestScore = 150
        )
        assertEquals(150, stats.highestScore)
    }
}
