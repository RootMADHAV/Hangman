package com.LetterQuest.domain.model

data class GlobalLeaderboardEntry(
    val userId: String,
    val username: String,
    val nickname: String,
    val avatarId: String,
    val value: Float,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val updatedAt: Long,
    val rank: Int = 0
) {
    val displayValue: String
        get() = when {
            value == Float.MAX_VALUE -> "—"
            value == 0f -> "0"
            value < 1f && value > 0f -> String.format("%.0f%%", value * 100)
            else -> String.format("%.0f", value)
        }
}
