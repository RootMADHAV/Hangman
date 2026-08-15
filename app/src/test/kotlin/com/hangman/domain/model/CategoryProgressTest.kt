package com.LetterQuest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryProgressTest {

    private val category = WordCategory("animals", "Animals", "🐾")

    private fun progress(
        gamesPlayed: Int = 0,
        gamesWon: Int = 0,
        bestScore: Int = 0,
        totalWords: Int = 20,
        solved: Int = 0
    ) = CategoryProgress(
        category = category,
        gamesPlayed = gamesPlayed,
        gamesWon = gamesWon,
        bestScore = bestScore,
        totalWordsInCategory = totalWords,
        distinctWordsSolved = solved
    )

    @Test
    fun testUnplayedCategoryHasZeroRates() {
        val unplayed = progress()

        assertTrue(unplayed.isUnplayed)
        assertEquals(0f, unplayed.winRate, 0.001f)
        assertEquals(0f, unplayed.completionRatio, 0.001f)
        assertFalse(unplayed.isMastered)
    }

    @Test
    fun testWinRateIsComputed() {
        val played = progress(gamesPlayed = 10, gamesWon = 7)

        assertEquals(0.7f, played.winRate, 0.001f)
    }

    @Test
    fun testCompletionRatioIsComputed() {
        val played = progress(gamesPlayed = 5, gamesWon = 5, totalWords = 20, solved = 5)

        assertEquals(0.25f, played.completionRatio, 0.001f)
    }

    @Test
    fun testMasteryRequiresEveryWord() {
        val nearly = progress(gamesPlayed = 30, gamesWon = 19, totalWords = 20, solved = 19)
        val mastered = progress(gamesPlayed = 30, gamesWon = 20, totalWords = 20, solved = 20)

        assertFalse(nearly.isMastered)
        assertTrue(mastered.isMastered)
        assertEquals(1f, mastered.completionRatio, 0.001f)
    }

    @Test
    fun testEmptyCategoryIsNotMastered() {
        val empty = progress(totalWords = 0, solved = 0)

        assertFalse("A category with no words cannot be mastered", empty.isMastered)
        assertEquals(0f, empty.completionRatio, 0.001f)
    }

    @Test
    fun testPlayedCategoryIsNotUnplayed() {
        assertFalse(progress(gamesPlayed = 1).isUnplayed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeGamesPlayedIsRejected() {
        progress(gamesPlayed = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWinsExceedingGamesIsRejected() {
        progress(gamesPlayed = 3, gamesWon = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSolvingMoreWordsThanExistIsRejected() {
        progress(gamesPlayed = 30, gamesWon = 25, totalWords = 20, solved = 21)
    }

    @Test
    fun testLossesDoNotCountAsSolved() {
        val allLosses = progress(gamesPlayed = 8, gamesWon = 0, totalWords = 20, solved = 0)

        assertEquals(0f, allLosses.winRate, 0.001f)
        assertEquals(0f, allLosses.completionRatio, 0.001f)
    }
}
