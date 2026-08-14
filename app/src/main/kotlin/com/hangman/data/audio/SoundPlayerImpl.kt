package com.hangman.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.hangman.domain.usecase.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the game's sound effects using tones synthesized at runtime.
 *
 * The previous implementation held five hardcoded `-1` sound IDs and never called
 * `SoundPool.load()`, so every play method was a silent no-op. There are no audio assets
 * in the project to load, so [ToneSynthesizer] generates the waveforms instead — which
 * also removes the need to inject a `Context`.
 *
 * Each playback allocates its own [AudioTrack] and releases it once the sound finishes.
 * That keeps overlapping sounds independent and makes [release] cheap.
 */
@Singleton
class SoundPlayerImpl @Inject constructor() : SoundPlayer {

    private val synthesizer = ToneSynthesizer()

    // Synthesis is pure arithmetic but not free, so each waveform is rendered once on
    // first use and then reused for the lifetime of the process.
    private val correctGuess by lazy { synthesizer.synthesize(GameTones.CORRECT) }
    private val incorrectGuess by lazy { synthesizer.synthesize(GameTones.INCORRECT) }
    private val win by lazy { synthesizer.synthesize(GameTones.WIN) }
    private val lose by lazy { synthesizer.synthesize(GameTones.LOSE) }
    private val buttonClick by lazy { synthesizer.synthesize(GameTones.CLICK) }

    override suspend fun playCorrectGuessSound() = play(correctGuess)

    override suspend fun playIncorrectGuessSound() = play(incorrectGuess)

    override suspend fun playWinSound() = play(win)

    override suspend fun playLoseSound() = play(lose)

    override suspend fun playButtonClickSound() = play(buttonClick)

    /**
     * No persistent audio resources are held between plays — every [AudioTrack] is
     * released as soon as its sound completes — so there is nothing to tear down here.
     *
     * This is deliberate. `SoundPlayer` is a `@Singleton` but `SoundViewModel` is not:
     * `GameplayScreen` and `GameResultsScreen` each obtain their own instance, so the
     * first one cleared would previously have released shared state out from under the
     * other. Keeping playback state per-call avoids that entirely.
     */
    override fun release() = Unit

    private suspend fun play(samples: ShortArray) = withContext(Dispatchers.Default) {
        val track = buildTrack(samples.size) ?: return@withContext
        try {
            track.write(samples, 0, samples.size)
            track.play()
            // Hold the track alive for the length of the sound; releasing early truncates it.
            delay(synthesizer.durationMsOf(samples) + RELEASE_GRACE_MS)
            track.stop()
        } catch (e: IllegalStateException) {
            // The track can be torn down by the platform mid-play (audio focus loss,
            // device disconnect). A missed sound effect is not worth surfacing.
        } finally {
            track.release()
        }
    }

    private fun buildTrack(sampleCount: Int): AudioTrack? = runCatching {
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(ToneSynthesizer.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(sampleCount * BYTES_PER_SAMPLE)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    }.getOrNull()

    private companion object {
        const val BYTES_PER_SAMPLE = 2
        const val RELEASE_GRACE_MS = 50L
    }
}
