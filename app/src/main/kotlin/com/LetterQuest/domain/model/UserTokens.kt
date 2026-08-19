package com.LetterQuest.domain.model

/**
 * The player's spendable hint currency.
 *
 * Tokens are earned by playing and spent on [HintType]s. The balance is clamped at
 * zero — spending more than is held is rejected by the repository rather than
 * producing a negative balance.
 */
@JvmInline
value class UserTokens(val balance: Int) {
    init {
        require(balance >= 0) { "Token balance cannot be negative, was $balance" }
    }

    fun canAfford(cost: Int): Boolean = balance >= cost

    companion object {
        /** New players start with 0 tokens. */
        const val STARTING_BALANCE = 0

        /** Awarded only when a word is actually guessed (not during mid-game individual letters).
         *  +1 per correct letter in the word, so longer words pay more — roughly 3 for typical
          simple words. */
        fun earnedPerWord(letters: Int): Int = letters.coerceAtLeast(1)

        /** Extra +1 per row of consecutive correct guesses inside one word (combo). */
        const val COMBO_STEP_TOKENS = 1

        /** Base flat reward for a win in Classic / Category Challenge mode. */
        const val EARNED_PER_CLASSIC_WIN = 4
        /** Base flat reward for a win in Timed Blitz mode (per word). */
        const val EARNED_PER_TIMED_WORD = 5
        /** Base flat reward for a win in Limited Guesses mode. */
        const val EARNED_PER_LIMITED_WIN = 6
        /** Max total reward for completing the daily challenge. */
        const val EARNED_PER_DAILY_WIN = 20
    }
}
