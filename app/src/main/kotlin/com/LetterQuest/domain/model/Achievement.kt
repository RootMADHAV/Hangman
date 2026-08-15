package com.LetterQuest.domain.model

/**
 * Canonical achievement model. Static definition data (name, description, icon,
 * reward) comes from [AchievementCatalog]; unlock state is persisted in Room and
 * merged onto the catalog entries by the repository layer.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String? = null,
    val condition: String = "",
    val rewardTokens: Int = 0,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false
)
