package com.LetterQuest.domain.usecase

/**
 * Controls looping background music. Tracks are switched explicitly so home and
 * gameplay BGM never overlap. Short SFX are handled separately by [SoundPlayer].
 */
interface MusicPlayer {
    fun startHome()
    fun startGameplay()
    fun stop()
    fun pause()
    fun resume()
    fun release()
    fun resumeLast()
}
