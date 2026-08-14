package com.hangman.domain.usecase

import com.hangman.domain.model.CategoryProgress
import com.hangman.domain.repository.GameHistoryRepository
import com.hangman.domain.repository.WordRepository
import javax.inject.Inject

/**
 * Builds per-category progress by joining game history against the word catalog.
 *
 * Progress is computed on demand rather than persisted: history is the single source of
 * truth, so there is no second copy of the data to keep consistent.
 */
class CategoryProgressCalculator @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameHistoryRepository: GameHistoryRepository
) {
    suspend fun calculate(): Result<List<CategoryProgress>> {
        return try {
            val categories = wordRepository.getCategories().getOrThrow()
            val allWords = wordRepository.getAllWords().getOrThrow()
            val history = gameHistoryRepository.getAllGames().getOrThrow()

            val gamesByCategory = history.groupBy { it.category }
            val wordCountByCategory = allWords.groupingBy { it.category }.eachCount()

            val progress = categories.map { category ->
                val games = gamesByCategory[category.id].orEmpty()
                val wins = games.filter { it.won }

                CategoryProgress(
                    category = category,
                    gamesPlayed = games.size,
                    gamesWon = wins.size,
                    bestScore = games.maxOfOrNull { it.score } ?: 0,
                    totalWordsInCategory = wordCountByCategory[category.id] ?: 0,
                    // Counted on uppercase values because history stores whatever
                    // casing the catalog used, and the same word must not count twice.
                    distinctWordsSolved = wins.map { it.word.uppercase() }.toSet().size
                )
            }

            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
