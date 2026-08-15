package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameState
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.ShopItem
import com.LetterQuest.domain.model.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorMultiplierTest {

    private fun wonGame(difficulty: Difficulty, word: String = "TESTING") = GameStatus(
        word = Word(word, difficulty),
        state = GameState.WON,
        guessedLetters = word.toSet(),
        remainingAttempts = difficulty.maxAttempts
    )

    @Test
    fun testLostGameScoresZero() {
        val lost = GameStatus(
            word = Word("TESTING", Difficulty.MEDIUM),
            state = GameState.LOST
        )

        assertEquals(0, ScoreCalculator.calculateScore(lost))
    }

    @Test
    fun testMediumOutscoresEasyForTheSameWord() {
        val easy = ScoreCalculator.calculateScore(wonGame(Difficulty.EASY))
        val medium = ScoreCalculator.calculateScore(wonGame(Difficulty.MEDIUM))

        // Regression: the multiplier was cast to Int, collapsing MEDIUM's 1.5x to 1x
        // and making it score the same as EASY for an equal-length word.
        assertTrue(
            "MEDIUM ($medium) should outscore EASY ($easy)",
            medium > easy
        )
    }

    @Test
    fun testHardOutscoresMedium() {
        val medium = ScoreCalculator.calculateScore(wonGame(Difficulty.MEDIUM))
        val hard = ScoreCalculator.calculateScore(wonGame(Difficulty.HARD))

        assertTrue("HARD ($hard) should outscore MEDIUM ($medium)", hard > medium)
    }

    @Test
    fun testScoreBoostIncreasesAward() {
        val base = ScoreCalculator.calculateScore(wonGame(Difficulty.MEDIUM))
        val boosted = ScoreCalculator.calculateScore(
            wonGame(Difficulty.MEDIUM),
            ShopItem.SCORE_BOOST_MULTIPLIER
        )

        assertTrue("Boosted score $boosted should exceed base $base", boosted > base)
        assertEquals(Math.round(base * ShopItem.SCORE_BOOST_MULTIPLIER), boosted)
    }

    @Test
    fun testDefaultMultiplierLeavesScoreUnchanged() {
        val implicit = ScoreCalculator.calculateScore(wonGame(Difficulty.HARD))
        val explicit = ScoreCalculator.calculateScore(wonGame(Difficulty.HARD), 1f)

        assertEquals(implicit, explicit)
    }

    @Test
    fun testLongerWordScoresHigher() {
        val short = ScoreCalculator.calculateScore(wonGame(Difficulty.EASY, "CAT"))
        val long = ScoreCalculator.calculateScore(wonGame(Difficulty.EASY, "ELEPHANT"))

        assertTrue(long > short)
    }

    @Test
    fun testBoostDoesNotApplyToLosses() {
        val lost = GameStatus(
            word = Word("TESTING", Difficulty.HARD),
            state = GameState.LOST
        )

        assertEquals(0, ScoreCalculator.calculateScore(lost, ShopItem.SCORE_BOOST_MULTIPLIER))
    }

    @Test
    fun testProgressionRespectsFractionalMultiplier() {
        val single = ScoreCalculator.calculateScoreProgression(7, 5, 1.0f)
        val oneAndAHalf = ScoreCalculator.calculateScoreProgression(7, 5, 1.5f)

        assertTrue(
            "A 1.5x multiplier must not truncate to 1x",
            oneAndAHalf > single
        )
    }
}
