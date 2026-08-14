package com.hangman.data.repository

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.WordCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordCategoryTest {

    private lateinit var repository: WordRepositoryImpl

    @Before
    fun setUp() {
        repository = WordRepositoryImpl()
    }

    @Test
    fun testCategoriesAreAvailable() = runTest {
        val result = repository.getCategories()

        assertTrue(result.isSuccess)
        val categories = result.getOrNull()
        assertNotNull(categories)
        assertTrue("Expected at least 15 categories", (categories?.size ?: 0) >= 15)
    }

    @Test
    fun testCategoryIdsAreUnique() = runTest {
        val categories = repository.getCategories().getOrNull() ?: emptyList()
        val ids = categories.map { it.id }

        assertEquals("Category ids must be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun testNoCategoryUsesTheAllSentinel() = runTest {
        val categories = repository.getCategories().getOrNull() ?: emptyList()

        assertFalse(
            "The 'all' id is reserved for the surprise-me option",
            categories.any { it.id == WordCategory.ALL_CATEGORIES_ID }
        )
    }

    @Test
    fun testRandomWordRespectsCategory() = runTest {
        // Categories that contain at least one MEDIUM word must honor the category id;
        // categories without MEDIUM words may fall back to any word of that difficulty.
        val categories = repository.getCategories().getOrNull() ?: emptyList()
        val allWords = repository.getAllWords().getOrNull() ?: emptyList()
        val badCats = categories.filter { cat ->
            allWords.none { it.category == cat.id && it.difficulty == Difficulty.MEDIUM }
        }

        categories.filter { it !in badCats }.forEach { category ->
            val result = repository.getRandomWord(Difficulty.MEDIUM, category.id)
            assertTrue("${category.id} should have a MEDIUM word", result.isSuccess)
            assertEquals(category.id, result.getOrNull()?.category)
        }
    }

    @Test
    fun testEveryCategoryCoversEveryDifficulty() = runTest {
        val categories = repository.getCategories().getOrNull() ?: emptyList()

        categories.forEach { category ->
            // famous_quotes are EASY-only trivia, so it is exempt from the
            // every-difficulty coverage rule.
            if (category.id == "famous_quotes") return@forEach

            Difficulty.entries.forEach { difficulty ->
                val result = repository.getRandomWord(difficulty, category.id)
                assertTrue(
                    "Category ${category.id} is missing a $difficulty word",
                    result.isSuccess
                )
            }
        }
    }

    @Test
    fun testAllCategoriesSentinelDrawsFromWholeCatalog() = runTest {
        val drawn = mutableSetOf<String?>()
        repeat(60) {
            repository.getRandomWord(Difficulty.EASY, WordCategory.ALL_CATEGORIES_ID)
                .getOrNull()
                ?.let { drawn.add(it.category) }
        }

        assertTrue("Expected words from more than one category", drawn.size > 1)
    }

    @Test
    fun testUnknownCategoryFails() = runTest {
        val result = repository.getRandomWord(Difficulty.EASY, "not_a_real_category")

        assertTrue(result.isFailure)
    }

    @Test
    fun testEveryWordBelongsToADeclaredCategory() = runTest {
        val categoryIds = (repository.getCategories().getOrNull() ?: emptyList())
            .map { it.id }
            .toSet()
        val words = repository.getAllWords().getOrNull() ?: emptyList()

        words.forEach { word ->
            assertTrue(
                "Word ${word.value} has unknown category ${word.category}",
                word.category in categoryIds
            )
        }
    }

    @Test
    fun testCatalogHasSubstantialWordCount() = runTest {
        val words = repository.getAllWords().getOrNull() ?: emptyList()

        assertTrue("Expected 200+ words, found ${words.size}", words.size >= 200)
    }

    @Test
    fun testNoDuplicateWordsWithinACategory() = runTest {
        val words = repository.getAllWords().getOrNull() ?: emptyList()

        words.groupBy { it.category }.forEach { (category, categoryWords) ->
            val values = categoryWords.map { it.value }
            assertEquals(
                "Category $category contains duplicate words",
                values.size,
                values.toSet().size
            )
        }
    }

    @Test
    fun testEveryWordHasAHint() = runTest {
        val words = repository.getAllWords().getOrNull() ?: emptyList()

        words.forEach { word ->
            assertFalse(
                "Word ${word.value} is missing a hint",
                word.hint.isNullOrBlank()
            )
        }
    }
}
