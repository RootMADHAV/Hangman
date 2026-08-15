package com.LetterQuest.domain.model

data class PlayerProfile(
    val playerId: String = "",
    val nickname: String = "Player",
    val avatarId: String = "avatar_1",
    val totalGamesPlayed: Int = 0,
    val totalTokensEarned: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis()
)

data class AvatarOption(
    val id: String,
    val displayName: String,
    val emoji: String,
    val description: String
)

object AvatarCatalog {
    val avatars = listOf(
        AvatarOption("avatar_1", "Player", "🎮", "Default player avatar"),
        AvatarOption("avatar_2", "Champion", "🏆", "Champion trophy"),
        AvatarOption("avatar_3", "Genius", "🧠", "Smart brain"),
        AvatarOption("avatar_4", "Speed Demon", "⚡", "Lightning fast"),
        AvatarOption("avatar_5", "Lucky", "🍀", "Lucky clover"),
        AvatarOption("avatar_6", "Dragon", "🐉", "Powerful dragon"),
        AvatarOption("avatar_7", "Phoenix", "🔥", "Rising phoenix"),
        AvatarOption("avatar_8", "Knight", "⚔️", "Brave knight"),
        AvatarOption("avatar_9", "Wizard", "🧙", "Magical wizard"),
        AvatarOption("avatar_10", "Star", "⭐", "Shining star")
    )

    fun getAvatarById(id: String): AvatarOption? =
        avatars.find { it.id == id }

    fun getDefaultAvatar(): AvatarOption =
        avatars.firstOrNull() ?: AvatarOption("avatar_1", "Player", "🎮", "Default player avatar")
}
