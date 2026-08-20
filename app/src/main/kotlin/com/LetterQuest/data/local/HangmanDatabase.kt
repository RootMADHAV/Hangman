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
    version = 7,
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

        /** Adds uuid and updatedAt columns for cloud sync. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_history ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE game_history ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds unique index on uuid to prevent duplicate game history entries. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_game_history_uuid ON game_history (uuid)")
            }
        }

        /** Adds hintsUsed column to track hint usage per game for achievements. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_history ADD COLUMN hintsUsed INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds gameMode column to track game mode for achievements. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_history ADD COLUMN gameMode TEXT NOT NULL DEFAULT 'CLASSIC'")
            }
        }
    }
}
