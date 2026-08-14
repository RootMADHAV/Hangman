package com.hangman.data.audio

import android.content.Context
import android.media.MediaPlayer
import com.hangman.domain.usecase.MusicPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the optional looping `bgm` resource. No-ops silently when the resource is
 * absent or playback fails — background music must never crash gameplay.
 */
@Singleton
class MusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayer {

    private var player: MediaPlayer? = null

    override fun start() {
        if (player?.isPlaying == true) return
        try {
            val resId = context.resources.getIdentifier(BGM_RESOURCE_NAME, "raw", context.packageName)
            if (resId == 0) return
            player?.release()
            player = MediaPlayer.create(context, resId)?.apply {
                isLooping = true
                setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME)
                start()
            }
        } catch (_: Exception) {
            // A missing/corrupt track is not worth surfacing to the user.
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun release() = stop()

    private companion object {
        const val BGM_RESOURCE_NAME = "bgm"
        const val DEFAULT_VOLUME = 0.4f
    }
}
