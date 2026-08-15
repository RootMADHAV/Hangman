package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.Word
import com.LetterQuest.domain.model.WordCategory

interface WordRepository {
    suspend fun getRandomWord(difficulty: Difficulty): Result<Word>
    suspend fun getAllWords(): Result<List<Word>>
    suspend fun getWordsByDifficulty(difficulty: Difficulty): Result<List<Word>>

    suspend fun getCategories(): Result<List<WordCategory>>

    /**
     * Draws a random word from [categoryId] at [difficulty]. Passing
     * [WordCategory.ALL_CATEGORIES_ID] draws from the whole catalog.
     */
    suspend fun getRandomWord(difficulty: Difficulty, categoryId: String): Result<Word>

    /**
     * Draws a random word from [categoryId] at [difficulty], excluding [excludeWord]
     * to prevent immediate repetition.
     */
    suspend fun getRandomWordExcluding(
        difficulty: Difficulty,
        categoryId: String,
        excludeWord: String
    ): Result<Word>
}
