package com.LetterQuest.domain.model

data class DailyLoginReward(
    val lastClaimedDateKey: String? = null,
    val currentStreak: Int = 0,
    val canClaim: Boolean = false,
    val tokensReward: Int = REWARDS[0]
) {
    companion object {
        val REWARDS = intArrayOf(10, 15, 20, 30, 40, 50, 70)
        val MAX_STREAK_DAYS = REWARDS.size

        fun rewardForStreak(streak: Int): Int {
            val index = ((streak - 1).coerceAtLeast(0)) % MAX_STREAK_DAYS
            return REWARDS[index]
        }
    }
}
