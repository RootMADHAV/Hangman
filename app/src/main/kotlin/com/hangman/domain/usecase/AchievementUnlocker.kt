package com.hangman.domain.usecase

import com.hangman.domain.model.GameState
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.PlayerStatistics
import com.hangman.domain.repository.AchievementRepository
import com.hangman.domain.repository.DailyChallengeRepository
import com.hangman.domain.repository.GameHistoryRepository
import com.hangman.domain.repository.TokenRepository
import javax.inject.Inject

class AchievementUnlocker @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val tokenRepository: TokenRepository,
    private val dailyChallengeRepository: DailyChallengeRepository
) {
    companion object {
        private const val FAST_SOLVE_SECONDS = 30
        private const val STREAK_FIVE = 5
        private const val STREAK_TEN = 10
        private const val LUCKY_SEVEN_COUNT = 7
        private const val COMEBACK_REMAINING_ATTEMPTS = 1
        private const val CATEGORY_WINS_TARGET = 5
        private const val TOKEN_HOARD_TARGET = 1000
    }

    /**
     * @param usedHint kept for API compatibility with the call site; no current
     *   achievement depends on it.
     * @param isTimedWord one word inside a Timed Blitz session. Individual timed words
     *   skip the one-time narrative achievements (First Victory) which are reserved
     *   for the player's first real Classic/daily win.
     */
    suspend fun evaluateAchievements(
        gameStatus: GameStatus,
        statistics: PlayerStatistics,
        usedHint: Boolean = false,
        isTimedWord: Boolean = false
    ) {
        // Always-cheap lifetime counters; safe to run on every game end.
        checkLuckySeven(statistics)
        checkTokenCollector()

        if (gameStatus.state != GameState.WON) return

        if (!isTimedWord) {
            checkFirstWin(statistics)
            checkPerfectGuess(gameStatus)
            checkFastSolve(gameStatus)
        }
        checkStreaks()
        checkComebackKing(gameStatus)
        checkCategoryMaster()
    }

    /**
     * Unlocks challenge/daily achievements. Currently the catalog has no
     * daily-specific IDs, so this is a no-op placeholder; kept for future use.
     */
    suspend fun evaluateDailyAchievements() {
        // No daily achievements in the current catalog.
    }

    private suspend fun checkFirstWin(statistics: PlayerStatistics) {
        if (statistics.gamesWon >= 1) {
            achievementRepository.unlockAchievement("first_win")
        }
    }

    private suspend fun checkStreaks() {
        val maxStreak = gameHistoryRepository.getMaxWinStreak().getOrNull() ?: 0
        if (maxStreak >= STREAK_FIVE) {
            achievementRepository.unlockAchievement("five_streak")
        }
        if (maxStreak >= STREAK_TEN) {
            achievementRepository.unlockAchievement("ten_streak")
        }
    }

    private suspend fun checkPerfectGuess(gameStatus: GameStatus) {
        if (gameStatus.state == GameState.WON && gameStatus.incorrectGuesses.isEmpty()) {
            achievementRepository.unlockAchievement("perfect_accuracy")
        }
    }

    private suspend fun checkFastSolve(gameStatus: GameStatus) {
        if (gameStatus.state == GameState.WON && gameStatus.elapsedSeconds <= FAST_SOLVE_SECONDS) {
            achievementRepository.unlockAchievement("fast_solve")
        }
    }

    private suspend fun checkComebackKing(gameStatus: GameStatus) {
        if (gameStatus.state == GameState.WON &&
            gameStatus.remainingAttempts == COMEBACK_REMAINING_ATTEMPTS
        ) {
            achievementRepository.unlockAchievement("comeback_king")
        }
    }

    private suspend fun checkCategoryMaster() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull() ?: return
        val categories = wonGames.mapNotNull { it.category }.toSet()
        if (categories.size < 2) return // need at least a couple of categories before checking
        val allHaveFiveWins = categories.all { cat ->
            wonGames.count { it.category == cat } >= CATEGORY_WINS_TARGET
        }
        if (allHaveFiveWins) {
            achievementRepository.unlockAchievement("category_master")
        }
    }

    private suspend fun checkLuckySeven(statistics: PlayerStatistics) {
        if (statistics.gamesWon == LUCKY_SEVEN_COUNT) {
            achievementRepository.unlockAchievement("lucky_seven")
        }
    }

    private suspend fun checkTokenCollector() {
        val tokens = tokenRepository.getTokens().getOrNull() ?: return
        if (tokens.balance >= TOKEN_HOARD_TARGET) {
            achievementRepository.unlockAchievement("token_collector")
        }
    }
}
