package com.LetterQuest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTokensTest {

    @Test
    fun testCanAffordWhenBalanceExceedsCost() {
        val tokens = UserTokens(50)

        assertTrue(tokens.canAfford(10))
        assertTrue(tokens.canAfford(50))
    }

    @Test
    fun testCannotAffordWhenBalanceBelowCost() {
        val tokens = UserTokens(5)

        assertFalse(tokens.canAfford(10))
    }

    @Test
    fun testZeroBalanceIsValid() {
        assertEquals(0, UserTokens(0).balance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeBalanceIsRejected() {
        UserTokens(-1)
    }

    @Test
    fun testStartingBalanceIsZero() {
        // Players start with 0 tokens and earn through gameplay.
        assertEquals(0, UserTokens.STARTING_BALANCE)
    }

    @Test
    fun testPerWordRewardIsPositive() {
        assertTrue(UserTokens.earnedPerWord(1) > 0)
        assertTrue(UserTokens.earnedPerWord(5) > 0)
    }

    @Test
    fun testLongerWordsPayMore() {
        assertTrue(
            "Longer words should earn more tokens",
            UserTokens.earnedPerWord(6) > UserTokens.earnedPerWord(3)
        )
    }

    @Test
    fun testShortWordRewardMatchesUserSpec() {
        // "+"3 for a short word guess" — a simple 3-letter word earns 3 tokens.
        assertEquals(3, UserTokens.earnedPerWord(3))
    }

    @Test
    fun testWinRewardCoversARevealHint() {
        assertTrue("Win reward should cover at least one hint", UserTokens.EARNED_PER_WIN >= HintType.REVEAL_LETTER.cost)
    }

    @Test
    fun testWinRewardOutpacesLossReward() {
        assertTrue(UserTokens.EARNED_PER_WIN > UserTokens.EARNED_PER_LOSS)
        assertTrue(UserTokens.EARNED_PER_LOSS > 0)
    }

    @Test
    fun testComboStepIsPositive() {
        assertTrue(UserTokens.COMBO_STEP_TOKENS > 0)
    }

    @Test
    fun testHintCostsArePositive() {
        HintType.entries.forEach { hint ->
            assertTrue("${hint.name} should cost something", hint.cost > 0)
            assertTrue(hint.displayName.isNotBlank())
            assertTrue(hint.description.isNotBlank())
        }
    }
}
