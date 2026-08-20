package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.GameMode
import com.LetterQuest.domain.model.GameState
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.PlayerStatistics
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.ShopRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.usecase.GameplayConfig
import javax.inject.Inject

class AchievementUnlocker @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val tokenRepository: TokenRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val shopRepository: ShopRepository
) {
    companion object {
        private const val STREAK_FIVE = 5
        private const val STREAK_TEN = 10
        private const val LUCKY_SEVEN_COUNT = 7
        private const val COMEBACK_REMAINING_ATTEMPTS = 1
        private const val CATEGORY_WINS_TARGET = 5
        private const val TOKEN_HOARD_TARGET = 1000
        private const val PESOS_KING_TARGET = 5000
        private const val WEEKLY_CHALLENGE_TARGET = 7
        private const val HINT_ADDICT_COUNT = 5
        private const val SPEED_DEMON_WINS = 3
        private const val SPEED_DEMON_THRESHOLD = 10
        private const val NO_HINTS_WINS = 3
        private const val WORD_LEARNER_GAMES = 10
        private const val CATEGORY_EXPLORER_COUNT = 5
        private const val COMBO_MASTER_THRESHOLD = 15
        private const val DEDICATED_DAYS = 7
        private const val CENTURION_WINS = 100
        private const val UNBREAKABLE_STREAK = 20
        private const val DAILY_DEVOTEE_COUNT = 30
        private const val TYCOON_TOKENS = 10000
        private const val FLAWLESS_WINS = 10
        private const val SPEED_DEMON_HARD_WINS = 10
        private const val NO_HINTS_MASTER_WINS = 10
        private const val WORD_SCHOLAR_CATEGORIES = 10
        private const val COMBO_GOD_THRESHOLD = 30
        private const val MARATHON_RUNNER_WINS = 50
        private const val IRON_FIST_STREAK = 25
        private const val LEGENDARY_WINS = 500
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
        isTimedWord: Boolean = false,
        hintsUsedThisGame: Int = 0
    ) {
        checkLuckySeven(statistics)
        checkTokenCollector()
        checkPesosKing()
        checkRichMan()
        checkThroneBreaker()
        checkWordLearner(statistics)
        checkDedicated()

        if (gameStatus.state != GameState.WON) return

        if (!isTimedWord) {
            checkFirstWin(statistics)
            checkPerfectGuess(gameStatus)
            checkFastSolve(gameStatus)
        }
        checkStreaks()
        checkComebackKing(gameStatus)
        checkCategoryMaster()
        checkMarathon()
        checkHintAddict(hintsUsedThisGame)
        checkSpeedDemon()
        checkNoHints()
        checkWordExplorer()
        checkComboMaster(gameStatus)
        checkCenturion(statistics)
        checkUnbreakable()
        checkDailyDevotee()
        checkTycoon()
        checkFlawless()
        checkSpeedDemonHard()
        checkNoHintsMaster()
        checkWordScholar()
        checkComboGod(gameStatus)
        checkMarathonRunner()
        checkPuzzleMaster()
        checkIronFist()
        checkLegendary(statistics)
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
        if (gameStatus.state == GameState.WON && gameStatus.elapsedSeconds <= GameplayConfig.FAST_SOLVE_THRESHOLD_SECONDS) {
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

    private suspend fun checkPesosKing() {
        val tokens = tokenRepository.getTokens().getOrNull() ?: return
        if (tokens.balance >= PESOS_KING_TARGET) {
            achievementRepository.unlockAchievement("pesosking")
        }
    }

    private suspend fun checkRichMan() {
        if (shopRepository.hasMadeIAP()) {
            achievementRepository.unlockAchievement("richman")
        }
    }

    private suspend fun checkThroneBreaker() {
        val streak = dailyChallengeRepository.getStreak().getOrNull() ?: return
        if (streak.longest >= WEEKLY_CHALLENGE_TARGET) {
            achievementRepository.unlockAchievement("thronebreaker")
        }
    }

    private suspend fun checkWordLearner(statistics: PlayerStatistics) {
        if (statistics.gamesPlayed >= WORD_LEARNER_GAMES) {
            achievementRepository.unlockAchievement("word_learner")
        }
    }

    private suspend fun checkMarathon() {
        val allGames = gameHistoryRepository.getAllGames().getOrNull().orEmpty()
        var currentStreak = 0
        var bestStreak = 0
        for (game in allGames) {
            if (game.won && game.gameMode == GameMode.TIMED.name) {
                currentStreak++
                if (currentStreak > bestStreak) bestStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }
        if (bestStreak >= STREAK_FIVE) {
            achievementRepository.unlockAchievement("marathon")
        }
    }

    private suspend fun checkHintAddict(hintsUsed: Int) {
        if (hintsUsed >= HINT_ADDICT_COUNT) {
            achievementRepository.unlockAchievement("hint_addict")
        }
    }

    private suspend fun checkSpeedDemon() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val fastWins = wonGames.count { it.elapsedSeconds <= SPEED_DEMON_THRESHOLD }
        if (fastWins >= SPEED_DEMON_WINS) {
            achievementRepository.unlockAchievement("speed_demon")
        }
    }

    private suspend fun checkNoHints() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val noHintWins = wonGames.count { it.hintsUsed == 0 }
        if (noHintWins >= NO_HINTS_WINS) {
            achievementRepository.unlockAchievement("no_hints")
        }
    }

    private suspend fun checkWordExplorer() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val categories = wonGames.mapNotNull { it.category }.toSet()
        if (categories.size >= CATEGORY_EXPLORER_COUNT) {
            achievementRepository.unlockAchievement("word_explorer")
        }
    }

    private suspend fun checkComboMaster(gameStatus: GameStatus) {
        if (gameStatus.maxCombo >= COMBO_MASTER_THRESHOLD) {
            achievementRepository.unlockAchievement("combo_master")
        }
    }

    private suspend fun checkDedicated() {
        val games = gameHistoryRepository.getAllGames().getOrNull().orEmpty()
        val playDays = games.map { it.playedAt / 86_400_000L }.toSet()
        if (playDays.size >= DEDICATED_DAYS) {
            achievementRepository.unlockAchievement("dedicated")
        }
    }

    private suspend fun checkCenturion(statistics: PlayerStatistics) {
        if (statistics.gamesWon >= CENTURION_WINS) {
            achievementRepository.unlockAchievement("centurion")
        }
    }

    private suspend fun checkUnbreakable() {
        val maxStreak = gameHistoryRepository.getMaxWinStreak().getOrNull() ?: 0
        if (maxStreak >= UNBREAKABLE_STREAK) {
            achievementRepository.unlockAchievement("unbreakable")
        }
    }

    private suspend fun checkDailyDevotee() {
        val streak = dailyChallengeRepository.getStreak().getOrNull() ?: return
        if (streak.current >= DAILY_DEVOTEE_COUNT) {
            achievementRepository.unlockAchievement("daily_devotee")
        }
    }

    private suspend fun checkTycoon() {
        val tokens = tokenRepository.getTokens().getOrNull() ?: return
        if (tokens.balance >= TYCOON_TOKENS) {
            achievementRepository.unlockAchievement("tycoon")
        }
    }

    private suspend fun checkFlawless() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val flawlessWins = wonGames.count { it.totalGuesses == it.correctGuesses }
        if (flawlessWins >= FLAWLESS_WINS) {
            achievementRepository.unlockAchievement("flawless")
        }
    }

    private suspend fun checkSpeedDemonHard() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val fastWins = wonGames.count { it.elapsedSeconds <= GameplayConfig.FAST_SOLVE_THRESHOLD_SECONDS }
        if (fastWins >= SPEED_DEMON_HARD_WINS) {
            achievementRepository.unlockAchievement("speed_demon_hard")
        }
    }

    private suspend fun checkNoHintsMaster() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val noHintWins = wonGames.count { it.hintsUsed == 0 }
        if (noHintWins >= NO_HINTS_MASTER_WINS) {
            achievementRepository.unlockAchievement("no_hints_master")
        }
    }

    private suspend fun checkWordScholar() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val categories = wonGames.mapNotNull { it.category }.toSet()
        if (categories.size >= WORD_SCHOLAR_CATEGORIES) {
            achievementRepository.unlockAchievement("word_scholar")
        }
    }

    private suspend fun checkComboGod(gameStatus: GameStatus) {
        if (gameStatus.maxCombo >= COMBO_GOD_THRESHOLD) {
            achievementRepository.unlockAchievement("combo_god")
        }
    }

    private suspend fun checkMarathonRunner() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val timedWins = wonGames.count { it.gameMode == GameMode.TIMED.name }
        if (timedWins >= MARATHON_RUNNER_WINS) {
            achievementRepository.unlockAchievement("marathon_runner")
        }
    }

    private suspend fun checkPuzzleMaster() {
        val wonGames = gameHistoryRepository.getWonGames().getOrNull().orEmpty()
        val perfectWins = wonGames.count { it.totalGuesses == it.correctGuesses }
        if (perfectWins >= FLAWLESS_WINS) {
            achievementRepository.unlockAchievement("puzzle_master")
        }
    }

    private suspend fun checkIronFist() {
        val maxStreak = gameHistoryRepository.getMaxWinStreak().getOrNull() ?: 0
        if (maxStreak >= IRON_FIST_STREAK) {
            achievementRepository.unlockAchievement("iron_fist")
        }
    }

    private suspend fun checkLegendary(statistics: PlayerStatistics) {
        if (statistics.gamesWon >= LEGENDARY_WINS) {
            achievementRepository.unlockAchievement("legendary")
        }
    }
}
