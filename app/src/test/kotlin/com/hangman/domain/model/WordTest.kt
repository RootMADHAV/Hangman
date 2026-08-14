package com.hangman.domain.model

import org.junit.Assert.*
import org.junit.Test

class WordTest {

    @Test
    fun testValidWord() {
        val word = Word("HANGMAN", Difficulty.MEDIUM)
        assertEquals("HANGMAN", word.value)
        assertEquals(Difficulty.MEDIUM, word.difficulty)
    }

    @Test
    fun testNormalizedValue() {
        val word = Word("hangman", Difficulty.EASY)
        assertEquals("HANGMAN", word.normalizedValue)
    }

    @Test
    fun testWordWithHint() {
        val word = Word("HANGMAN", Difficulty.MEDIUM, "A word guessing game")
        assertEquals("A word guessing game", word.hint)
    }

    @Test
    fun testWordWithCategory() {
        val word = Word("HANGMAN", Difficulty.MEDIUM, category = "Games")
        assertEquals("Games", word.category)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankWordThrows() {
        Word("", Difficulty.EASY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSpaceWordThrows() {
        Word("   ", Difficulty.EASY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNumbersInWordThrows() {
        Word("HANG123", Difficulty.EASY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSpecialCharInWordThrows() {
        Word("HANG-MAN", Difficulty.EASY)
    }

    @Test
    fun testValidLettersOnlyWord() {
        val word = Word("ABCDEFGHIJKLMNOPQRSTUVWXYZ", Difficulty.HARD)
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", word.value)
    }
}
