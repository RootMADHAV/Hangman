package com.hangman.domain.model

/**
 * A player's record within a single word category.
 *
 * Derived from game history rather than stored, so it always reflects the actual games
 * played and cannot drift out of sync with them.
 */
data class CategoryProgress(
    val category: WordCategory,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val bestScore: Int,
    val totalWordsInCategory: Int,
    val distinctWordsSolved: Int
) {
    init {
        require(gamesPlayed >= 0) { "Games played cannot be negative" }
        require(gamesWon in 0..gamesPlayed) { "Wins cannot exceed games played" }
        require(distinctWordsSolved <= totalWordsInCategory) {
            "Cannot solve more words than the category contains"
        }
    }

    val winRate: Float
        get() = if (gamesPlayed == 0) 0f else gamesWon.toFloat() / gamesPlayed

    /** Fraction of the category's words the player has solved at least once, 0f..1f. */
    val completionRatio: Float
        get() = if (totalWordsInCategory == 0) 0f
        else distinctWordsSolved.toFloat() / totalWordsInCategory

    val isMastered: Boolean
        get() = totalWordsInCategory > 0 && distinctWordsSolved == totalWordsInCategory

    val isUnplayed: Boolean
        get() = gamesPlayed == 0
}
