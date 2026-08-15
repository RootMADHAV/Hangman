package com.LetterQuest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ShopItemTest {

    @Test
    fun testAllItemsHaveDistinctIds() {
        val ids = ShopItem.entries.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun testAllItemsHavePositiveCost() {
        assertTrue(ShopItem.entries.all { it.cost > 0 })
    }

    @Test
    fun testAllItemsHaveDisplayText() {
        ShopItem.entries.forEach { item ->
            assertTrue(item.displayName.isNotBlank())
            assertTrue(item.description.isNotBlank())
            assertTrue(item.icon.isNotBlank())
        }
    }

    @Test
    fun testFromIdResolvesEveryItem() {
        ShopItem.entries.forEach { item ->
            assertEquals(item, ShopItem.fromId(item.id))
        }
    }

    @Test
    fun testFromIdReturnsNullForUnknownId() {
        assertEquals(null, ShopItem.fromId("not_a_real_item"))
    }

    @Test
    fun testDiscountReducesHintCost() {
        val discounted = Math.round(
            HintType.SKIP_WORD.cost * ShopItem.HINT_DISCOUNT_MULTIPLIER
        )

        assertTrue("Discount must lower the price", discounted < HintType.SKIP_WORD.cost)
        assertTrue("Discounted hints must still cost something", discounted > 0)
    }

    @Test
    fun testScoreBoostIncreasesScore() {
        assertTrue(ShopItem.SCORE_BOOST_MULTIPLIER > 1f)
    }

    @Test
    fun testPerksCostMoreThanASingleHint() {
        val priciestHint = HintType.entries.maxOf { it.cost }

        // Perks are permanent, so they should be a meaningful saving decision rather
        // than an impulse buy at hint prices.
        assertTrue(ShopItem.entries.all { it.cost > priciestHint })
    }

    @Test
    fun testEveryPerkIsReachableByPlaying() {
        // With the win-based economy: a 5-letter word win + win bonus + a small combo
        // payout per game is enough that even the priciest perk is reachable within a
        // reasonable winstreak.
        val tokensPerWord = UserTokens.EARNED_PER_WIN + UserTokens.earnedPerWord(5) + UserTokens.COMBO_STEP_TOKENS * 2
        val priciest = ShopItem.entries.maxOf { it.cost }
        val wordsNeeded = (priciest + tokensPerWord - 1) / tokensPerWord
        assertTrue("Priciest perk reachable within ~100 wins", wordsNeeded < 100)
    }

    @Test
    fun testDiscountIsNotFree() {
        HintType.entries.forEach { hint ->
            val discounted = Math.round(hint.cost * ShopItem.HINT_DISCOUNT_MULTIPLIER)
            assertTrue("$hint must still cost tokens when discounted", discounted >= 1)
        }
    }

    @Test
    fun testItemsAreOrderedByAscendingCost() {
        val costs = ShopItem.entries.map { it.cost }

        assertEquals(
            "Shop items should be declared cheapest-first",
            costs.sorted(),
            costs
        )
    }

    @Test
    fun testExtraLifeRemovedFromShop() {
        // The extra-life power-up was moved out of the shop into Classic gameplay.
        assertNull(ShopItem.fromId("extra_life_pack"))
    }
}
