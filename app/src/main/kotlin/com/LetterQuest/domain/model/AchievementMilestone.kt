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
        ),
        AchievementMilestone(
            id = "word_learner",
            name = "Word Learner",
            description = "Play 10 games",
            icon = "📚",
            condition = "games_played >= 10",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "marathon",
            name = "Marathon",
            description = "Reach a 5-game winning streak in Timed mode",
            icon = "🏃",
            condition = "timed_streak >= 5",
            rewardTokens = 15
        ),
        AchievementMilestone(
            id = "hint_addict",
            name = "Hint Addict",
            description = "Use 5 hints in one game",
            icon = "💡",
            condition = "hints_used >= 5",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "speed_demon",
            name = "Speed Demon",
            description = "Win 3 games in under 10 seconds each",
            icon = "⚡",
            condition = "fast_wins >= 3",
            rewardTokens = 15
        ),
        AchievementMilestone(
            id = "no_hints",
            name = "Pure Skill",
            description = "Win 3 games without using hints",
            icon = "🧠",
            condition = "no_hint_wins >= 3",
            rewardTokens = 15
        ),
        AchievementMilestone(
            id = "word_explorer",
            name = "Word Explorer",
            description = "Win games in 5 different categories",
            icon = "🗺️",
            condition = "category_wins >= 5",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "combo_master",
            name = "Combo Master",
            description = "Reach a 15x combo in Timed mode",
            icon = "🔥",
            condition = "max_combo >= 15",
            rewardTokens = 15
        ),
        AchievementMilestone(
            id = "dedicated",
            name = "Dedicated",
            description = "Play on 7 different days",
            icon = "📅",
            condition = "play_days >= 7",
            rewardTokens = 10
        ),
        AchievementMilestone(
            id = "centurion",
            name = "Centurion",
            description = "Win 100 games",
            icon = "💯",
            condition = "games_won >= 100",
            rewardTokens = 100
        ),
        AchievementMilestone(
            id = "unbreakable",
            name = "Unbreakable",
            description = "Reach a 20-game winning streak",
            icon = "🛡️",
            condition = "max_streak >= 20",
            rewardTokens = 50
        ),
        AchievementMilestone(
            id = "daily_devotee",
            name = "Daily Devotee",
            description = "Complete 30 daily challenges",
            icon = "📆",
            condition = "daily_completions >= 30",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "tycoon",
            name = "Tycoon",
            description = "Accumulate 10000 tokens",
            icon = "👑",
            condition = "total_tokens >= 10000",
            rewardTokens = 50
        ),
        AchievementMilestone(
            id = "flawless",
            name = "Flawless",
            description = "Win 10 games with no wrong guesses",
            icon = "💎",
            condition = "perfect_wins >= 10",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "speed_demon_hard",
            name = "Speed Demon",
            description = "Win 10 games in under 5 seconds each",
            icon = "⚡⚡",
            condition = "fast_wins >= 10",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "no_hints_master",
            name = "No Hints Master",
            description = "Win 10 games without using hints",
            icon = "🧠🧠",
            condition = "no_hint_wins >= 10",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "word_scholar",
            name = "Word Scholar",
            description = "Win games in 10 different categories",
            icon = "🎓",
            condition = "category_wins >= 10",
            rewardTokens = 20
        ),
        AchievementMilestone(
            id = "combo_god",
            name = "Combo God",
            description = "Reach a 30x combo in Timed mode",
            icon = "🔥🔥",
            condition = "max_combo >= 30",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "marathon_runner",
            name = "Marathon Runner",
            description = "Win 50 games in Timed mode",
            icon = "🏃‍♂️",
            condition = "timed_wins >= 50",
            rewardTokens = 50
        ),
        AchievementMilestone(
            id = "puzzle_master",
            name = "Puzzle Master",
            description = "Win 10 games with perfect accuracy",
            icon = "🧩",
            condition = "perfect_wins >= 10",
            rewardTokens = 30
        ),
        AchievementMilestone(
            id = "iron_fist",
            name = "Iron Fist",
            description = "Win 25 games in a row",
            icon = "👊",
            condition = "max_streak >= 25",
            rewardTokens = 50
        ),
        AchievementMilestone(
            id = "legendary",
            name = "Legendary",
            description = "Win 500 games total",
            icon = "👑👑",
            condition = "games_won >= 500",
            rewardTokens = 200
        )
    )

    fun getAchievementById(id: String): AchievementMilestone? =
        achievements.find { it.id == id }
}
