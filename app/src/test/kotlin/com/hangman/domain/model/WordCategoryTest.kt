package com.LetterQuest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordCategoryTest {

    @Test
    fun testValidCategoryIsConstructed() {
        val category = WordCategory("animals", "Animals", "🐾")

        assertEquals("animals", category.id)
        assertEquals("Animals", category.name)
        assertEquals("🐾", category.icon)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankIdIsRejected() {
        WordCategory("", "Animals", "🐾")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankNameIsRejected() {
        WordCategory("animals", "  ", "🐾")
    }

    @Test
    fun testAllCategoriesSentinelIsDefined() {
        assertTrue(WordCategory.ALL_CATEGORIES_ID.isNotBlank())
    }
}
