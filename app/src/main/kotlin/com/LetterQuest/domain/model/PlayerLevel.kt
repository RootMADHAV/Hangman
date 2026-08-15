package com.LetterQuest.domain.model

/**
 * Player level derived from games won and total score — no extra persistence needed.
 *
 * XP = gamesWon * 10 + totalScore / 10
 * XP to reach level n = 50 * (n-1)²   → level 2 = 50 XP, level 3 = 200, level 4 = 450 …
 */
data class PlayerLevel(
    val level: Int,
    val title: String,
    val currentXp: Int,
    val xpForNextLevel: Int,
    /** 0.0 = start of level, 1.0 = level complete. */
    val progress: Float
) {
    companion object {
        fun from(gamesWon: Int, totalScore: Int): PlayerLevel {
            val xp = gamesWon * 10 + totalScore / 10
            var level = 1
            while (xpForLevel(level + 1) <= xp) level++
            val xpStart = xpForLevel(level)
            val xpEnd = xpForLevel(level + 1)
            val range = xpEnd - xpStart
            return PlayerLevel(
                level = level,
                title = titleFor(level),
                currentXp = xp - xpStart,
                xpForNextLevel = range,
                progress = if (range > 0) (xp - xpStart).toFloat() / range else 1f
            )
        }

        private fun xpForLevel(n: Int): Int = 50 * (n - 1) * (n - 1)

        private fun titleFor(level: Int): String = when {
            level >= 50 -> "Almighty"
            level >= 40 -> "Thronebreaker"
            level >= 30 -> "Legend"
            level >= 25 -> "Master"
            level >= 20 -> "Expert"
            level >= 15 -> "Pro"
            level >= 10 -> "Veteran"
            level >= 7  -> "Skilled"
            level >= 5  -> "Noob"
            level >= 3  -> "Novice"
            level >= 2  -> "Newbie"
            else        -> "Beginner"
        }
    }
}
