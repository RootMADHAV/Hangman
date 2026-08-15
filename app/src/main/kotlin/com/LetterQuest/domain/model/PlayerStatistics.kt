package com.LetterQuest.domain.model

data class PlayerStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalScore: Int = 0,
    val highestScore: Int = 0,
    val averageScore: Float = 0f,
    val winRate: Float = 0f
) {
    val winPercentage: Float
        get() = if (gamesPlayed > 0) (gamesWon.toFloat() / gamesPlayed.toFloat()) * 100f else 0f

    val averageScoreCalculated: Float
        get() = if (gamesPlayed > 0) totalScore.toFloat() / gamesPlayed.toFloat() else 0f
}
