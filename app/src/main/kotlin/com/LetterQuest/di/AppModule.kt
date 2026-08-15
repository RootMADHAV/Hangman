package com.LetterQuest.di

import com.LetterQuest.data.audio.MusicPlayerImpl
import com.LetterQuest.data.audio.SoundPlayerImpl
import com.LetterQuest.domain.usecase.MusicPlayer
import com.LetterQuest.domain.usecase.SoundPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSoundPlayer(impl: SoundPlayerImpl): SoundPlayer

    @Binds
    @Singleton
    abstract fun bindMusicPlayer(impl: MusicPlayerImpl): MusicPlayer
}
