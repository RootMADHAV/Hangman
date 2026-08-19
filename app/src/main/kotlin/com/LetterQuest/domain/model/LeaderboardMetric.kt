package com.LetterQuest.domain.model

enum class LeaderboardMetric(val title: String, val field: String) {
    TOTAL_SCORE("Total Score", "totalScore"),
    GAMES_WON("Games Won", "gamesWon"),
    WIN_RATE("Win Rate", "winRate");

    val minGamesForWinRate: Int
        get() = if (this == WIN_RATE) 5 else 0
}
