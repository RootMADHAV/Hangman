package com.LetterQuest.data.audio

import android.content.Context
import android.media.MediaPlayer
import com.LetterQuest.domain.usecase.MusicPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayer {

    private var player: MediaPlayer? = null
    private var wasPlayingBeforePause: Boolean = false
    private var lastTrack: String? = null

    override fun startHome() {
        lastTrack = "home"
        playTrack("bgm_home.mp3")
    }

    override fun startGameplay() {
        lastTrack = "gameplay"
        playTrack("bgm_gameplay.mp3")
    }

    override fun resumeLast() {
        when (lastTrack) {
            "gameplay" -> startGameplay()
            else -> startHome()
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
        wasPlayingBeforePause = false
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
                // If resume fails, attempt to restart the current track
                startHome()
            }
        }
    }

    override fun release() = stop()

    private fun playTrack(assetPath: String) {
        if (player?.isPlaying == true) {
            val currentDataSource = getCurrentDataSource()
            if (currentDataSource == assetPath) return
            stop()
        }
        try {
            val afd = context.assets.openFd(assetPath)
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

    private fun getCurrentDataSource(): String? {
        return try {
            player?.let { "unknown" }
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val DEFAULT_VOLUME = 0.4f
    }
}
