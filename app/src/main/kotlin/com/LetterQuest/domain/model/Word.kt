package com.LetterQuest.domain.model

data class Word(
    val value: String,
    val difficulty: Difficulty,
    val hint: String? = null,
    val category: String? = null
) {
    init {
        require(value.isNotBlank()) { "Word cannot be blank" }
        require(value.all { it.isLetter() || it.isWhitespace() }) { "Word must contain only letters and spaces" }
    }

    val normalizedValue: String
        get() = value.uppercase()

    val displayValue: String
        get() = normalizedValue.replace(" ", " / ")

    /** True when a non-blank clue is attached to this word. */
    val hasClue: Boolean
        get() = !hint.isNullOrBlank()

    /** The clue as user-facing text, or null when none exists. Alias of [hint]. */
    val clue: String?
        get() = hint?.takeIf { it.isNotBlank() }
}
