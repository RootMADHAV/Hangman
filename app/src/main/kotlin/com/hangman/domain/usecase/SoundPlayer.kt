package com.hangman.domain.usecase

interface SoundPlayer {
    suspend fun playCorrectGuessSound()
    suspend fun playIncorrectGuessSound()
    suspend fun playWinSound()
    suspend fun playLoseSound()
    suspend fun playButtonClickSound()
    fun release()
}
