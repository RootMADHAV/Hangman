package com.LetterQuest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.LetterQuest.domain.model.PlayerStatistics

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey
    val id: Int = 1,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalScore: Int = 0,
    val highestScore: Int = 0,
    val averageScore: Float = 0f,
    val winRate: Float = 0f
) {
    fun toPlayerStatistics(): PlayerStatistics {
        return PlayerStatistics(
            gamesPlayed = gamesPlayed,
            gamesWon = gamesWon,
            gamesLost = gamesLost,
            totalScore = totalScore,
            highestScore = highestScore,
            averageScore = averageScore,
            winRate = winRate
        )
    }

    companion object {
        fun fromPlayerStatistics(stats: PlayerStatistics): StatisticsEntity {
            return StatisticsEntity(
                gamesPlayed = stats.gamesPlayed,
                gamesWon = stats.gamesWon,
                gamesLost = stats.gamesLost,
                totalScore = stats.totalScore,
                highestScore = stats.highestScore,
                averageScore = stats.averageScore,
                winRate = stats.winRate
            )
        }
    }
}
