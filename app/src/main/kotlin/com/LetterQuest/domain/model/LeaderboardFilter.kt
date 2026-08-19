package com.LetterQuest.domain.model

enum class LeaderboardSortBy {
    SCORE_DESC,
    TIME_ASC,
    DATE_DESC,
    CATEGORY;

    val displayName: String
        get() = when (this) {
            SCORE_DESC -> "Highest Score"
            TIME_ASC -> "Fastest Time"
            DATE_DESC -> "Most Recent"
            CATEGORY -> "By Category"
        }
}

enum class LeaderboardTimeFilter {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME;

    val displayName: String
        get() = when (this) {
            TODAY -> "Today"
            THIS_WEEK -> "This Week"
            THIS_MONTH -> "This Month"
            ALL_TIME -> "All Time"
        }
}

enum class LeaderboardDisplayMode {
    ALL,
    INDIVIDUAL_WINS,
    STREAKS;

    val displayName: String
        get() = when (this) {
            ALL -> "All Entries"
            INDIVIDUAL_WINS -> "Individual Wins"
            STREAKS -> "Streak Totals"
        }
}

data class LeaderboardFilterConfig(
    val sortBy: LeaderboardSortBy = LeaderboardSortBy.SCORE_DESC,
    val timeFilter: LeaderboardTimeFilter = LeaderboardTimeFilter.ALL_TIME,
    val selectedCategory: String? = null,
    val difficultyFilter: Difficulty? = null,
    val displayMode: LeaderboardDisplayMode = LeaderboardDisplayMode.ALL
)
