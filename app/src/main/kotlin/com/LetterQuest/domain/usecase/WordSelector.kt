package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.Word
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.domain.repository.WordRepository
import javax.inject.Inject

/**
 * Selects words from the catalog. The repository already surfaces errors as [Result]
 * failures, so these methods delegate directly without extra try/catch wrapping.
 */
class WordSelector @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend fun selectRandomWord(difficulty: Difficulty): Result<Word> =
        selectRandomWord(difficulty, WordCategory.ALL_CATEGORIES_ID)

    suspend fun selectRandomWord(difficulty: Difficulty, categoryId: String): Result<Word> =
        wordRepository.getRandomWord(difficulty, categoryId)

    /** Selects a random word, excluding [excludeWord] to prevent immediate repetition. */
    suspend fun selectRandomWordExcluding(
        difficulty: Difficulty,
        categoryId: String,
        excludeWord: String
    ): Result<Word> =
        wordRepository.getRandomWordExcluding(difficulty, categoryId, excludeWord)

    /**
     * Classic-mode word source: prefers words that carry a clue (Classic shows the
     * clue up-front as the level prompt). [excludeWord], when given, avoids an
     * immediate repeat of the just-played word. Falls back to any matching word when
     * the pool of clued words is empty.
     */
    suspend fun selectWordWithClue(
        difficulty: Difficulty,
        categoryId: String,
        excludeWord: String? = null
    ): Result<Word> {
        val poolResult = wordRepository.getAllWords()
        val pool = poolResult.getOrNull().orEmpty().filter { word ->
            word.difficulty == difficulty &&
                word.hasClue &&
                (categoryId == WordCategory.ALL_CATEGORIES_ID || word.category == categoryId) &&
                word.normalizedValue != excludeWord?.uppercase()
        }
        if (pool.isNotEmpty()) {
            return Result.success(pool.random())
        }

        // No clued word fits — fall back to the standard random pick so the level
        // can still be played (the clue banner simply won't render).
        return if (excludeWord != null) {
            selectRandomWordExcluding(difficulty, categoryId, excludeWord)
        } else {
            selectRandomWord(difficulty, categoryId)
        }
    }

    suspend fun getCategories(): Result<List<WordCategory>> =
        wordRepository.getCategories()

    suspend fun getWordsByDifficulty(difficulty: Difficulty): Result<List<Word>> =
        wordRepository.getWordsByDifficulty(difficulty)

    suspend fun getWordsForCategory(difficulty: Difficulty, categoryId: String): Result<List<Word>> =
        wordRepository.getAllWords().map { words ->
            words.filter { it.difficulty == difficulty &&
                (categoryId == WordCategory.ALL_CATEGORIES_ID || it.category == categoryId) }
        }
}
