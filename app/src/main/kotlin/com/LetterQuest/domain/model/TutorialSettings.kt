package com.LetterQuest.domain.model

data class TutorialSettings(
    val showGameplayTutorial: Boolean = true,
    val showHintsTutorial: Boolean = true,
    val showThemesTutorial: Boolean = true,
    val tutorialCompletedCount: Int = 0
)
