package com.hangman.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hangman.data.local.repository.AchievementRepositoryLocal
import com.hangman.data.local.repository.DailyChallengeRepositoryLocal
import com.hangman.data.local.repository.DailyLoginRepositoryLocal
import com.hangman.data.local.repository.GameHistoryRepositoryLocal
import com.hangman.data.local.repository.PreferencesRepositoryLocal
import com.hangman.data.local.repository.ShopRepositoryLocal
import com.hangman.data.local.repository.StatisticsRepositoryLocal
import com.hangman.data.local.repository.TokenRepositoryLocal
import com.hangman.data.repository.WordRepositoryImpl
import com.hangman.domain.repository.AchievementRepository
import com.hangman.domain.repository.DailyChallengeRepository
import com.hangman.domain.repository.DailyLoginRepository
import com.hangman.domain.repository.GameHistoryRepository
import com.hangman.domain.repository.PreferencesRepository
import com.hangman.domain.repository.ShopRepository
import com.hangman.domain.repository.StatisticsRepository
import com.hangman.domain.repository.TokenRepository
import com.hangman.domain.repository.WordRepository
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

    companion object {
        @Provides
        @Singleton
        fun provideTokenRepository(
            @ApplicationContext context: Context,
            dataStore: DataStore<Preferences>
        ): TokenRepository = TokenRepositoryLocal(dataStore, context)
    }
}
