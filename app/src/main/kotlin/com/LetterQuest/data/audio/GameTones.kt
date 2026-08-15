package com.LetterQuest.data.audio

import com.LetterQuest.data.audio.ToneSynthesizer.Note

/**
 * Note sequences for each gameplay event.
 *
 * Rising intervals signal success, falling intervals signal failure, so the five sounds
 * stay distinguishable from one another without relying on volume alone.
 */
internal object GameTones {

    /** Two rising notes (A5 → E6). */
    val CORRECT = listOf(
        Note(frequencyHz = 880.00, durationMs = 70),
        Note(frequencyHz = 1318.51, durationMs = 90)
    )

    /** Low falling buzz (A#3 → F#3). */
    val INCORRECT = listOf(
        Note(frequencyHz = 233.08, durationMs = 90, amplitude = 0.5),
        Note(frequencyHz = 185.00, durationMs = 150, amplitude = 0.5)
    )

    /** Major arpeggio (C5 → E5 → G5 → C6). */
    val WIN = listOf(
        Note(frequencyHz = 523.25, durationMs = 90),
        Note(frequencyHz = 659.25, durationMs = 90),
        Note(frequencyHz = 783.99, durationMs = 90),
        Note(frequencyHz = 1046.50, durationMs = 220)
    )

    /** Descending resolution (G4 → E4 → C4). */
    val LOSE = listOf(
        Note(frequencyHz = 392.00, durationMs = 140, amplitude = 0.5),
        Note(frequencyHz = 329.63, durationMs = 140, amplitude = 0.5),
        Note(frequencyHz = 261.63, durationMs = 260, amplitude = 0.5)
    )

    /** Short, quiet blip — fires on every letter press, so it stays unobtrusive. */
    val CLICK = listOf(
        Note(frequencyHz = 1200.00, durationMs = 25, amplitude = 0.35)
    )
}
