package com.LetterQuest.domain.usecase

/**
 * Controls looping background music. Separate from [SoundPlayer] so ViewModels can
 * toggle music without holding an Android `Context` (which leaks and breaks unit tests).
 */
interface MusicPlayer {
    fun start()
    fun stop()
    fun release()
}
