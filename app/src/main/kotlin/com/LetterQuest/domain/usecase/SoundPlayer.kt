package com.LetterQuest.domain.usecase

/**
 * Controls playback of the game's sound effects loaded from [res/raw].
 */
interface SoundPlayer {
    suspend fun playCorrectGuessSound()
    suspend fun playIncorrectGuessSound()
    suspend fun playGameOverSound()
    suspend fun playHintSound()
    suspend fun playLowLivesSound()
    fun release()
}
