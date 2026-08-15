package com.LetterQuest.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LetterQuest.data.local.HangmanDatabase
import com.LetterQuest.data.local.dao.AchievementDao
import com.LetterQuest.data.local.dao.GameHistoryDao
import com.LetterQuest.data.local.dao.StatisticsDao
import com.LetterQuest.data.local.repository.TokenRepositoryLocal
import com.LetterQuest.domain.repository.TokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideHangmanDatabase(
        @ApplicationContext context: Context
    ): HangmanDatabase {
        return Room.databaseBuilder(
            context,
            HangmanDatabase::class.java,
            "hangman_database"
        ).addMigrations(HangmanDatabase.MIGRATION_2_3)
            .build()
    }

    @Singleton
    @Provides
    fun provideStatisticsDao(database: HangmanDatabase): StatisticsDao {
        return database.statisticsDao()
    }

    @Singleton
    @Provides
    fun provideAchievementDao(database: HangmanDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Singleton
    @Provides
    fun provideGameHistoryDao(database: HangmanDatabase): GameHistoryDao {
        return database.gameHistoryDao()
    }

    @Singleton
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.preferencesDataStore
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
