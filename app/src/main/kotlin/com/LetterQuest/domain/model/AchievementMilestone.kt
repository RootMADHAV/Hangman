package com.LetterQuest.domain.model

data class AchievementMilestone(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val condition: String,
    val unlockedAt: Long? = null,
    val rewardTokens: Int = 0
) {
    val isUnlocked: Boolean
        get() = unlockedAt != null
}

object AchievementCatalog {
    val achievements = listOf(
        AchievementMilestone(
            id = "first_win",
            name = "First Victory",
            description = "Win your first game",
            icon = "🏆",
            condition = "games_won >= 1",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "five_streak",
            name = "On Fire!",
            description = "Reach a 5-game winning streak",
            icon = "🔥",
            condition = "max_streak >= 5",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "ten_streak",
            name = "Unstoppable",
            description = "Reach a 10-game winning streak",
            icon = "⚡",
            condition = "max_streak >= 10",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "perfect_accuracy",
            name = "Perfect Guess",
            description = "Win with no wrong guesses",
            icon = "✨",
            condition = "perfect_game",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "fast_solve",
            name = "Speed Racer",
            description = "Solve a word in under 5 seconds",
            icon = "🚀",
            condition = "fast_solve",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "token_collector",
            name = "Token Hoarder",
            description = "Accumulate 1000 tokens",
            icon = "💰",
            condition = "total_tokens >= 1000",
            rewardTokens = 0
        ),
        AchievementMilestone(
            id = "category_master",
            name = "Category Expert",
            description = "Win 5 games in each category",
            icon = "👑",
            condition = "all_categories_5wins",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "challenge_winner",
            name = "Challenge Master",
            description = "Win a challenge mode game",
            icon = "🎯",
            condition = "challenge_win",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "lucky_seven",
            name = "Lucky Number",
            description = "Win exactly 7 games",
            icon = "🍀",
            condition = "games_won == 7",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "comeback_king",
            name = "Comeback King",
            description = "Win with only 1 wrong guess remaining",
            icon = "💪",
            condition = "comeback_win",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "pesosking",
            name = "Peso King",
            description = "Accumulate 5000 tokens",
            icon = "👑",
            condition = "total_tokens >= 5000",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "richman",
            name = "Rich Man",
            description = "Make an in-app purchase",
            icon = "💎",
            condition = "made_iap",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "thronebreaker",
            name = "Thronebreaker",
            description = "Complete all daily challenges in a week",
            icon = "🏰",
            condition = "weekly_challenges_complete",
            rewardTokens = 10
        )
    )

    fun getAchievementById(id: String): AchievementMilestone? =
        achievements.find { it.id == id }
}
