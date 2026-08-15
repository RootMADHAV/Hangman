package com.LetterQuest.data.audio

import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates 16-bit mono PCM waveforms for the game's sound effects.
 *
 * The game ships no audio assets, so every sound is synthesized at runtime instead of
 * being loaded from `res/raw`. This class deliberately contains no Android dependencies
 * so the waveform logic can be unit-tested on the JVM; [SoundPlayerImpl] owns the
 * playback side.
 */
class ToneSynthesizer(private val sampleRate: Int = SAMPLE_RATE) {

    /** A single sine tone. [amplitude] is a 0..1 fraction of full scale. */
    data class Note(
        val frequencyHz: Double,
        val durationMs: Int,
        val amplitude: Double = 0.6
    )

    /** Renders [notes] back-to-back into one PCM buffer. */
    fun synthesize(notes: List<Note>): ShortArray {
        require(notes.isNotEmpty()) { "At least one note is required" }

        val samples = ShortArray(notes.sumOf { samplesFor(it.durationMs) })
        var offset = 0

        notes.forEach { note ->
            val count = samplesFor(note.durationMs)
            for (i in 0 until count) {
                val seconds = i.toDouble() / sampleRate
                val wave = sin(2.0 * PI * note.frequencyHz * seconds)
                val scaled = wave * note.amplitude * envelopeAt(i, count) * Short.MAX_VALUE
                samples[offset + i] = scaled
                    .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                    .toInt()
                    .toShort()
            }
            offset += count
        }

        return samples
    }

    /** Playback duration of a buffer produced by [synthesize]. */
    fun durationMsOf(samples: ShortArray): Long =
        samples.size.toLong() * 1000L / sampleRate

    private fun samplesFor(durationMs: Int): Int {
        require(durationMs > 0) { "Note duration must be positive, was $durationMs" }
        return sampleRate * durationMs / 1000
    }

    /**
     * Linear attack/release ramp. Starting or ending a note at a non-zero sample value
     * produces an audible click, so each note is faded in and out.
     */
    private fun envelopeAt(index: Int, total: Int): Double {
        val ramp = (total * RAMP_FRACTION).toInt().coerceAtLeast(1)
        return when {
            index < ramp -> index.toDouble() / ramp
            index >= total - ramp -> (total - index - 1).toDouble() / ramp
            else -> 1.0
        }
    }

    companion object {
        const val SAMPLE_RATE = 44100
        private const val RAMP_FRACTION = 0.15
    }
}
