package com.hangman.data.audio

import com.hangman.data.audio.ToneSynthesizer.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ToneSynthesizerTest {

    private val synthesizer = ToneSynthesizer()

    @Test
    fun testSampleCountMatchesDuration() {
        val samples = synthesizer.synthesize(listOf(Note(440.0, 100)))

        // 44100 Hz * 100 ms
        assertEquals(4410, samples.size)
    }

    @Test
    fun testNotesAreConcatenated() {
        val samples = synthesizer.synthesize(
            listOf(Note(440.0, 100), Note(880.0, 50))
        )

        assertEquals(4410 + 2205, samples.size)
    }

    @Test
    fun testHonoursCustomSampleRate() {
        val samples = ToneSynthesizer(sampleRate = 8000).synthesize(listOf(Note(440.0, 100)))

        assertEquals(800, samples.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyNoteListIsRejected() {
        synthesizer.synthesize(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNonPositiveDurationIsRejected() {
        synthesizer.synthesize(listOf(Note(440.0, 0)))
    }

    @Test
    fun testWaveformStartsAndEndsAtSilence() {
        val samples = synthesizer.synthesize(listOf(Note(440.0, 100)))

        // A non-zero first or last sample is an audible click.
        assertEquals(0, samples.first().toInt())
        assertEquals(0, samples.last().toInt())
    }

    @Test
    fun testAmplitudeIsRespected() {
        val quiet = synthesizer.synthesize(listOf(Note(440.0, 100, amplitude = 0.25)))
        val loud = synthesizer.synthesize(listOf(Note(440.0, 100, amplitude = 0.9)))

        val quietPeak = quiet.maxOf { abs(it.toInt()) }
        val loudPeak = loud.maxOf { abs(it.toInt()) }

        assertTrue("Quiet peak $quietPeak should stay under its 0.25 ceiling",
            quietPeak <= (Short.MAX_VALUE * 0.25).toInt())
        assertTrue("Louder note should peak higher", loudPeak > quietPeak)
    }

    @Test
    fun testWaveformIsNotSilent() {
        val samples = synthesizer.synthesize(listOf(Note(440.0, 100)))

        assertTrue("Synthesized tone should contain non-zero samples",
            samples.any { it.toInt() != 0 })
    }

    @Test
    fun testDurationMsOfRoundTrips() {
        val samples = synthesizer.synthesize(listOf(Note(440.0, 200)))

        // Integer sample counts make this exact only to within one sample.
        assertTrue(abs(synthesizer.durationMsOf(samples) - 200L) <= 1L)
    }

    @Test
    fun testAllGameTonesProduceAudio() {
        val tones = mapOf(
            "correct" to GameTones.CORRECT,
            "incorrect" to GameTones.INCORRECT,
            "win" to GameTones.WIN,
            "lose" to GameTones.LOSE,
            "click" to GameTones.CLICK
        )

        tones.forEach { (name, notes) ->
            val samples = synthesizer.synthesize(notes)
            assertTrue("$name should produce samples", samples.isNotEmpty())
            assertTrue("$name should not be silent", samples.any { it.toInt() != 0 })
        }
    }

    @Test
    fun testGameTonesAreDistinguishable() {
        val correct = synthesizer.synthesize(GameTones.CORRECT)
        val incorrect = synthesizer.synthesize(GameTones.INCORRECT)
        val win = synthesizer.synthesize(GameTones.WIN)
        val lose = synthesizer.synthesize(GameTones.LOSE)

        // The player must be able to tell success from failure by ear.
        assertNotEquals(correct.toList(), incorrect.toList())
        assertNotEquals(win.toList(), lose.toList())
    }

    @Test
    fun testClickIsQuietestTone() {
        val click = synthesizer.synthesize(GameTones.CLICK).maxOf { abs(it.toInt()) }
        val correct = synthesizer.synthesize(GameTones.CORRECT).maxOf { abs(it.toInt()) }

        // Click fires on every letter press, so it must not dominate the mix.
        assertTrue("Click peak $click should be below correct-guess peak $correct",
            click < correct)
    }
}
