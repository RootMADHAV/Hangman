package com.LetterQuest.domain.model

data class CategoryStatistics(
    val categoryId: String,
    val categoryName: String,
    val totalGames: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalScore: Int = 0,
    val averageScore: Float = 0f,
    val accuracyPercentage: Float = 0f,
    val totalTimeSeconds: Long = 0,
    val averageTimeSeconds: Float = 0f,
    val bestScore: Int = 0,
    val difficulty: Map<String, Int> = emptyMap() // difficulty name to count
) {
    val winRate: Float
        get() = if (totalGames == 0) 0f else (gamesWon.toFloat() / totalGames) * 100

    fun updateWithGame(
        score: Int,
        timeSeconds: Long,
        isWin: Boolean,
        difficulty: String
    ): CategoryStatistics {
        val newTotalGames = totalGames + 1
        val newGamesWon = if (isWin) gamesWon + 1 else gamesWon
        val newTotalScore = totalScore + score
        val newBestScore = if (score > bestScore) score else bestScore
        val newTotalTime = totalTimeSeconds + timeSeconds

        val newDifficulty = this.difficulty.toMutableMap().apply {
            put(difficulty, (get(difficulty) ?: 0) + 1)
        }.toMap()

        return CategoryStatistics(
            categoryId = categoryId,
            categoryName = categoryName,
            totalGames = newTotalGames,
            gamesWon = newGamesWon,
            gamesLost = totalGames + 1 - newGamesWon,
            totalScore = newTotalScore,
            averageScore = newTotalScore.toFloat() / newTotalGames,
            accuracyPercentage = ((newGamesWon.toFloat() / newTotalGames) * 100),
            totalTimeSeconds = newTotalTime,
            averageTimeSeconds = newTotalTime.toFloat() / newTotalGames,
            bestScore = newBestScore,
            difficulty = newDifficulty
        )
    }
}

data class AllCategoryStatistics(
    val categoryStats: List<CategoryStatistics> = emptyList()
) {
    fun getStatsByCategory(categoryId: String): CategoryStatistics? =
        categoryStats.find { it.categoryId == categoryId }

    fun getSortedByWinRate(): List<CategoryStatistics> =
        categoryStats.sortedByDescending { it.winRate }

    fun getSortedByAverageScore(): List<CategoryStatistics> =
        categoryStats.sortedByDescending { it.averageScore }

    fun getSortedByTotalGames(): List<CategoryStatistics> =
        categoryStats.sortedByDescending { it.totalGames }
}
