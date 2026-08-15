package com.LetterQuest.domain.model

import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.UserPreferences
import org.junit.Assert.*
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun testDefaultPreferences() {
        val preferences = UserPreferences()
        assertTrue(preferences.soundEnabled)
        assertFalse(preferences.darkTheme)
        assertEquals(Difficulty.MEDIUM, preferences.defaultDifficulty)
        assertTrue(preferences.notificationsEnabled)
    }

    @Test
    fun testCustomPreferences() {
        val preferences = UserPreferences(
            soundEnabled = false,
            darkTheme = true,
            defaultDifficulty = Difficulty.HARD,
            notificationsEnabled = false
        )
        assertFalse(preferences.soundEnabled)
        assertTrue(preferences.darkTheme)
        assertEquals(Difficulty.HARD, preferences.defaultDifficulty)
        assertFalse(preferences.notificationsEnabled)
    }

    @Test
    fun testPreferencesCopy() {
        val original = UserPreferences(soundEnabled = true, darkTheme = false)
        val modified = original.copy(soundEnabled = false)
        assertTrue(original.soundEnabled)
        assertFalse(modified.soundEnabled)
    }
}
