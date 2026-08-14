package com.hangman.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hangman.domain.model.Achievement
import com.hangman.domain.model.AchievementMilestone

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String? = null,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false
) {
    fun toAchievement(): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            icon = icon,
            unlockedAt = unlockedAt,
            isUnlocked = isUnlocked
        )
    }

    companion object {
        fun fromAchievement(achievement: Achievement): AchievementEntity {
            return AchievementEntity(
                id = achievement.id,
                name = achievement.name,
                description = achievement.description,
                icon = achievement.icon,
                unlockedAt = achievement.unlockedAt,
                isUnlocked = achievement.isUnlocked
            )
        }
    }
}

/** Projects a catalog definition into a persistable entity seed row. */
fun AchievementMilestone.toEntity(
    isUnlocked: Boolean = false,
    unlockedAt: Long? = null
): AchievementEntity = AchievementEntity(
    id = id,
    name = name,
    description = description,
    icon = icon,
    unlockedAt = unlockedAt,
    isUnlocked = isUnlocked
)
