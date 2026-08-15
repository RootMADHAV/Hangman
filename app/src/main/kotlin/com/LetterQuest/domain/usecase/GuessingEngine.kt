package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.GameState
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.GuessResult

/**
 * Pure, stateless game-move processor. Given an immutable [GameStatus] and a guessed
 * letter, returns the resulting [GameStatus] together with the [GuessResult] describing
 * what happened. Contains no I/O, so it is trivially unit-testable.
 */
object GuessingEngine {

    fun processGuess(gameStatus: GameStatus, letter: Char): Pair<GameStatus, GuessResult> {
        if (gameStatus.isGameOver) {
            return gameStatus to GuessResult.Invalid
        }

        val normalizedLetter = letter.uppercaseChar()

        // Validate before the already-guessed check so a non-letter already present in a
        // guessed set is reported as Invalid rather than AlreadyGuessed.
        if (!normalizedLetter.isLetter()) {
            return gameStatus to GuessResult.Invalid
        }

        if (gameStatus.isLetterGuessed(normalizedLetter)) {
            return gameStatus to GuessResult.AlreadyGuessed
        }

        return if (normalizedLetter in gameStatus.word.normalizedValue) {
            handleCorrectGuess(gameStatus, normalizedLetter)
        } else {
            handleIncorrectGuess(gameStatus, normalizedLetter)
        }
    }

    private fun handleCorrectGuess(gameStatus: GameStatus, letter: Char): Pair<GameStatus, GuessResult> {
        val newCombo = gameStatus.currentCombo + 1
        val newStatus = gameStatus.copy(
            guessedLetters = gameStatus.guessedLetters + letter,
            currentCombo = newCombo,
            maxCombo = maxOf(gameStatus.maxCombo, newCombo)
        )
        val finished = if (newStatus.isWordComplete()) newStatus.copy(state = GameState.WON) else newStatus
        return finished to GuessResult.Correct
    }

    private fun handleIncorrectGuess(gameStatus: GameStatus, letter: Char): Pair<GameStatus, GuessResult> {
        val newRemaining = gameStatus.remainingAttempts - 1
        val newStatus = gameStatus.copy(
            incorrectGuesses = gameStatus.incorrectGuesses + letter,
            remainingAttempts = newRemaining,
            currentCombo = 0,
            state = if (newRemaining <= 0) GameState.LOST else GameState.PLAYING
        )
        return newStatus to GuessResult.Incorrect
    }

    fun validateLetter(letter: Char): Boolean = letter.isLetter()
}
