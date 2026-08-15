package com.LetterQuest.domain.model

data class ThemePreset(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val colors: ThemeColors,
    /** 0 = always free; >0 = must be purchased with tokens before use. */
    val cost: Int = 0
)

data class ThemeColors(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val error: Long,
    val onError: Long,
    val errorContainer: Long,
    val onErrorContainer: Long,
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val outline: Long
)
