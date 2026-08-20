package com.LetterQuest.data.audio

import android.content.Context
import android.media.MediaPlayer
import com.LetterQuest.domain.usecase.MusicPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the optional looping `bgm.mp3` from the assets folder. No-ops silently when
 * the resource is absent or playback fails — background music must never crash gameplay.
 */
@Singleton
class MusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayer {

    private var player: MediaPlayer? = null
    private var wasPlayingBeforePause: Boolean = false

    override fun start() {
        if (player?.isPlaying == true) return
        try {
            player?.release()
            val afd = context.assets.openFd("bgm.mp3")
            player = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME)
                prepare()
                start()
            }
        } catch (_: Exception) {
            player = null
        }
    }

    override fun stop() {
        try {
            player?.stop()
        } catch (_: Exception) {
            // ignore
        }
        player?.release()
        player = null
    }

    override fun pause() {
        if (player?.isPlaying == true) {
            wasPlayingBeforePause = true
            player?.pause()
        } else {
            wasPlayingBeforePause = false
        }
    }

    override fun resume() {
        val playerInstance = player
        if (wasPlayingBeforePause && playerInstance != null && !playerInstance.isPlaying) {
            try {
                player?.start()
            } catch (_: Exception) {
                start()
            }
        }
    }

    override fun release() = stop()

    private companion object {
        const val DEFAULT_VOLUME = 0.4f
    }
}
