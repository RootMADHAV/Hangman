package com.LetterQuest.domain.model

sealed class GuessResult {
    data object Correct : GuessResult()
    data object Incorrect : GuessResult()
    data object Invalid : GuessResult()
    data object AlreadyGuessed : GuessResult()
}
