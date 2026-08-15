package com.LetterQuest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.LetterQuest.data.local.dao.AchievementDao
import com.LetterQuest.data.local.dao.GameHistoryDao
import com.LetterQuest.data.local.dao.StatisticsDao
import com.LetterQuest.data.local.entity.AchievementEntity
import com.LetterQuest.data.local.entity.GameHistoryEntity
import com.LetterQuest.data.local.entity.StatisticsEntity

@Database(
    entities = [StatisticsEntity::class, AchievementEntity::class, GameHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HangmanDatabase : RoomDatabase() {
    abstract fun statisticsDao(): StatisticsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun gameHistoryDao(): GameHistoryDao

    companion object {
        /** Adds the category column recording which word category a game was played in. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_history ADD COLUMN category TEXT")
            }
        }
    }
}
