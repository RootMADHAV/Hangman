package com.LetterQuest.data.audio

import android.content.Context
import android.media.MediaPlayer
import com.LetterQuest.R
import com.LetterQuest.domain.usecase.SoundPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SoundPlayer {

    private val isSoundEnabled = MutableStateFlow(true)

    override suspend fun playCorrectGuessSound() = playRaw(R.raw.correct_guess)

    override suspend fun playIncorrectGuessSound() = playRaw(R.raw.wrong_guess)

    override suspend fun playGameOverSound() = playRaw(R.raw.gameover)

    override suspend fun playHintSound() = playRaw(R.raw.game_hint)

    override suspend fun playLowLivesSound() = playRaw(R.raw.low_lives_remain)

    override fun release() = Unit

    private suspend fun playRaw(resId: Int) {
        if (!isSoundEnabled.first()) return
        withTimeoutOrNull(3000L) {
            try {
                val player = MediaPlayer.create(context, resId) ?: return@withTimeoutOrNull
                player.setOnCompletionListener { it.release() }
                player.start()
            } catch (_: Exception) {
                // ignore playback failures
            }
        }
    }
}
