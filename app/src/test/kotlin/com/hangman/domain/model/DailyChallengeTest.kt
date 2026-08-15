package com.LetterQuest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyChallengeTest {

    @Test
    fun testDateKeyFormat() {
        val key = DailyChallenge.dateKeyFor(LocalDate.of(2026, 8, 6))

        assertEquals("2026-08-06", key)
    }

    @Test
    fun testDateKeyIsStableForSameDate() {
        val date = LocalDate.of(2026, 3, 15)

        assertEquals(
            DailyChallenge.dateKeyFor(date),
            DailyChallenge.dateKeyFor(LocalDate.of(2026, 3, 15))
        )
    }

    @Test
    fun testIndexIsDeterministicForSameKey() {
        val first = DailyChallenge.indexFor("2026-08-06", 100)
        val second = DailyChallenge.indexFor("2026-08-06", 100)

        assertEquals("Same date must always yield the same puzzle", first, second)
    }

    @Test
    fun testIndexStaysInBounds() {
        val sizes = listOf(1, 2, 7, 50, 231, 1000)

        sizes.forEach { size ->
            (1..28).forEach { day ->
                val key = DailyChallenge.dateKeyFor(LocalDate.of(2026, 2, day))
                val index = DailyChallenge.indexFor(key, size)

                assertTrue("Index $index out of bounds for size $size", index in 0 until size)
            }
        }
    }

    @Test
    fun testAdjacentDaysUsuallyDiffer() {
        val indices = (1..30).map { day ->
            DailyChallenge.indexFor(DailyChallenge.dateKeyFor(LocalDate.of(2026, 6, day)), 231)
        }

        // Not a strict guarantee for any single pair, but a month of identical puzzles
        // would mean the hash is not spreading at all.
        assertTrue("Expected varied puzzles across a month", indices.toSet().size > 20)
    }

    @Test
    fun testSingleWordCatalogAlwaysMapsToZero() {
        assertEquals(0, DailyChallenge.indexFor("2026-08-06", 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyCatalogIsRejected() {
        DailyChallenge.indexFor("2026-08-06", 0)
    }

    @Test
    fun testCompletionBonusIsWorthwhile() {
        assertTrue(
            "Daily bonus should be a meaningful reward",
            DailyChallenge.COMPLETION_BONUS_TOKENS > 0
        )
    }

    @Test
    fun testDifferentYearsProduceDifferentPuzzles() {
        val y2026 = DailyChallenge.indexFor("2026-08-06", 231)
        val y2027 = DailyChallenge.indexFor("2027-08-06", 231)

        assertNotEquals(y2026, y2027)
    }
}
