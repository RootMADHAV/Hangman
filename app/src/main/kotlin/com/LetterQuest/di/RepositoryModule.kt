package com.LetterQuest.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.LetterQuest.data.local.repository.AchievementRepositoryLocal
import com.LetterQuest.data.local.repository.DailyChallengeRepositoryLocal
import com.LetterQuest.data.local.repository.DailyLoginRepositoryLocal
import com.LetterQuest.data.local.repository.GameHistoryRepositoryLocal
import com.LetterQuest.data.local.repository.PreferencesRepositoryLocal
import com.LetterQuest.data.local.repository.ShopRepositoryLocal
import com.LetterQuest.data.local.repository.StatisticsRepositoryLocal
import com.LetterQuest.data.local.repository.SyncQueue
import com.LetterQuest.data.local.repository.TokenRepositoryLocal
import com.LetterQuest.data.repository.AuthRepositoryImpl
import com.LetterQuest.data.repository.CloudSyncRepositoryImpl
import com.LetterQuest.data.repository.LeaderboardRepositoryImpl
import com.LetterQuest.data.repository.WordRepositoryImpl
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.repository.CloudSyncRepository
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.DailyLoginRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.LeaderboardRepository
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.repository.ShopRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(impl: StatisticsRepositoryLocal): StatisticsRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryLocal): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryLocal): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindGameHistoryRepository(impl: GameHistoryRepositoryLocal): GameHistoryRepository

    @Binds
    @Singleton
    abstract fun bindDailyChallengeRepository(
        impl: DailyChallengeRepositoryLocal
    ): DailyChallengeRepository

    @Binds
    @Singleton
    abstract fun bindShopRepository(impl: ShopRepositoryLocal): ShopRepository

    @Binds
    @Singleton
    abstract fun bindDailyLoginRepository(impl: DailyLoginRepositoryLocal): DailyLoginRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCloudSyncRepository(impl: CloudSyncRepositoryImpl): CloudSyncRepository

    companion object {
        @Provides
        @Singleton
        fun provideLeaderboardRepository(
            impl: LeaderboardRepositoryImpl
        ): LeaderboardRepository = impl

        @Provides
        @Singleton
        fun provideTokenRepository(
            @ApplicationContext context: Context,
            dataStore: DataStore<Preferences>
        ): TokenRepository = TokenRepositoryLocal(dataStore, context)

        @Provides
        @Singleton
        fun provideSyncQueue(
            dataStore: DataStore<Preferences>,
            cloudSyncRepository: CloudSyncRepository
        ): SyncQueue = SyncQueue(dataStore, cloudSyncRepository)
    }
}
