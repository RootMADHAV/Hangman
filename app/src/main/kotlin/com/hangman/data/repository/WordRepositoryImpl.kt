package com.hangman.data.repository

import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.Word
import com.hangman.domain.model.WordCategory
import com.hangman.domain.repository.WordRepository
import javax.inject.Inject
import kotlin.random.Random

class WordRepositoryImpl @Inject constructor() : WordRepository {

    private val words = WordCatalog.words

    override suspend fun getRandomWord(difficulty: Difficulty): Result<Word> =
        getRandomWord(difficulty, WordCategory.ALL_CATEGORIES_ID)

    override suspend fun getRandomWord(difficulty: Difficulty, categoryId: String): Result<Word> {
        return try {
            // Prefer a word tagged with both the requested difficulty AND inside the
            // difficulty's length band. If the catalog has no in-band word for this
            // category (edge case), fall back to the difficulty tag only — but NEVER
            // leave the requested category.
            val inBand = words.filter { word ->
                word.difficulty == difficulty &&
                    word.normalizedValue.length in difficulty.wordLength &&
                    (categoryId == WordCategory.ALL_CATEGORIES_ID || word.category == categoryId)
            }
            val candidates = inBand.ifEmpty {
                words.filter { word ->
                    word.difficulty == difficulty &&
                        (categoryId == WordCategory.ALL_CATEGORIES_ID || word.category == categoryId)
                }
            }
            if (candidates.isEmpty()) {
                Result.failure(
                    IllegalArgumentException(
                        "No words available for difficulty $difficulty in category $categoryId"
                    )
                )
            } else {
                Result.success(candidates[Random.nextInt(candidates.size)])
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomWordExcluding(
        difficulty: Difficulty,
        categoryId: String,
        excludeWord: String
    ): Result<Word> {
        return try {
            val inBand = words.filter { word ->
                word.difficulty == difficulty &&
                    word.normalizedValue.length in difficulty.wordLength &&
                    (categoryId == WordCategory.ALL_CATEGORIES_ID || word.category == categoryId) &&
                    word.normalizedValue != excludeWord.uppercase()
            }
            val candidates = inBand.ifEmpty {
                words.filter { word ->
                    word.difficulty == difficulty &&
                        (categoryId == WordCategory.ALL_CATEGORIES_ID || word.category == categoryId) &&
                        word.normalizedValue != excludeWord.uppercase()
                }
            }
            if (candidates.isEmpty()) {
                // Fallback: if no other word exists, allow the excluded one
                getRandomWord(difficulty, categoryId)
            } else {
                Result.success(candidates[Random.nextInt(candidates.size)])
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllWords(): Result<List<Word>> = Result.success(words.distinctBy { w -> w.value + w.category })

    override suspend fun getWordsByDifficulty(difficulty: Difficulty): Result<List<Word>> {
        return try {
            Result.success(words.filter { it.difficulty == difficulty })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<WordCategory>> =
        Result.success(WordCatalog.categories)
}
