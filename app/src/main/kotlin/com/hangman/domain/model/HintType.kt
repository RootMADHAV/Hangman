package com.hangman.domain.model

/**
 * A purchasable in-game assist.
 *
 * Costs are balanced against [UserTokens.EARNED_PER_WIN]: a won game funds roughly one
 * reveal, so hints stay affordable without removing the challenge.
 */
enum class HintType(val cost: Int, val displayName: String, val description: String) {
    CLUE(
        cost = 8,
        displayName = "🔍 Clue",
        description = "Show the word's clue"
    ),
    REVEAL_LETTER(
        cost = 12,
        displayName = "🔤 Reveal",
        description = "Uncover one unguessed letter"
    ),
    REMOVE_WRONG_LETTERS(
        cost = 15,
        displayName = "❌ Clear Wrong",
        description = "Remove 2 wrong guessed letters"
    ),
    SKIP_WORD(
        cost = 20,
        displayName = "⏭ Skip",
        description = "Swap in a new word, keeping your score"
    ),
    EXTRA_LIFE(
        cost = 10,
        displayName = "❤️ Extra Life",
        description = "+1 attempt — Classic mode only"
    )
}
