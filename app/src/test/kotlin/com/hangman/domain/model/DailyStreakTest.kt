package com.hangman.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyStreakTest {

    @Test
    fun testDefaultStreakIsEmpty() {
        val streak = DailyStreak()

        assertEquals(0, streak.current)
        assertEquals(0, streak.longest)
        assertNull(streak.lastCompletedDateKey)
    }

    @Test
    fun testLongestMayExceedCurrent() {
        val streak = DailyStreak(current = 2, longest = 9, lastCompletedDateKey = "2026-08-06")

        assertEquals(2, streak.current)
        assertEquals(9, streak.longest)
    }

    @Test
    fun testEqualCurrentAndLongestIsValid() {
        val streak = DailyStreak(current = 5, longest = 5)

        assertEquals(streak.current, streak.longest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeCurrentIsRejected() {
        DailyStreak(current = -1, longest = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testLongestBelowCurrentIsRejected() {
        DailyStreak(current = 5, longest = 2)
    }
}
