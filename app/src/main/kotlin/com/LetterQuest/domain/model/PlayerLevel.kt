package com.LetterQuest.domain.model

import kotlin.math.pow

/**
 * Player level derived from games won and total score — no extra persistence needed.
 *
 * XP formula and thresholds are configurable via [config] so the progression curve
 * can be tuned without changing this class.
 *
 * Defaults:
 *   XP = gamesWon * 10 + totalScore / 10
 *   XP to reach level n = BASE_XP * (n-1) ^ EXPONENT
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
        data class Config(
            val gamesWonWeight: Int = 10,
            val totalScoreDivisor: Int = 10,
            val baseXp: Int = 50,
            val exponent: Float = 2f
        )

        val config: Config
            get() = Config()

        fun from(gamesWon: Int, totalScore: Int, cfg: Config = config): PlayerLevel {
            val xp = gamesWon * cfg.gamesWonWeight + totalScore / cfg.totalScoreDivisor
            var level = 1
            while (xpForLevel(level + 1, cfg) <= xp) level++
            val xpStart = xpForLevel(level, cfg)
            val xpEnd = xpForLevel(level + 1, cfg)
            val range = xpEnd - xpStart
            return PlayerLevel(
                level = level,
                title = titleFor(level),
                currentXp = xp - xpStart,
                xpForNextLevel = range,
                progress = if (range > 0) (xp - xpStart).toFloat() / range else 1f
            )
        }

        private fun xpForLevel(n: Int, cfg: Config): Int =
            (cfg.baseXp * (n - 1).toFloat().pow(cfg.exponent)).toInt()

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
