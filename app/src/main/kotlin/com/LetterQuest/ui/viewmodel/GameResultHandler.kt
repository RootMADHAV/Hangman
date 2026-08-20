package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.ChallengeMode
import com.LetterQuest.domain.model.ChallengeModeConfig
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.domain.usecase.ScoreCalculator
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.LeaderboardRepository
import com.LetterQuest.domain.repository.ShopRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.usecase.AchievementUnlocker
import com.LetterQuest.domain.usecase.CloudSyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pure business logic for end-of-game processing.
 *
 * Accepts all required inputs as parameters and returns a [GameResultOutcome].
 * Does not reference Compose or ViewModel types, making it easy to unit test.
 */
class GameResultHandler @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val achievementUnlocker: AchievementUnlocker,
    private val gameHistoryRepository: GameHistoryRepository,
    private val tokenRepository: TokenRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val shopRepository: ShopRepository,
    private val cloudSyncUseCase: CloudSyncUseCase,
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: com.LetterQuest.domain.repository.AuthRepository
) {
    suspend fun process(
        scope: CoroutineScope,
        gameStatus: GameStatus,
        currentSessionScore: Int,
        challengeConfig: ChallengeModeConfig,
        isDailyChallenge: Boolean,
        hintsUsedThisGame: Int,
        usedHintThisGame: Boolean
    ): GameResultOutcome {
        val perks = gameStatus.perks
        val scoreMultiplier = if (com.LetterQuest.domain.model.ShopItem.SCORE_BOOST in perks) {
            com.LetterQuest.domain.model.ShopItem.SCORE_BOOST_MULTIPLIER
        } else {
            1f
        } * challengeConfig.scoreMultiplier
        val score = ScoreCalculator.calculateScore(gameStatus, scoreMultiplier)
        val won = gameStatus.state == com.LetterQuest.domain.model.GameState.WON
        statisticsRepository.recordGameResult(won, score)

        val newSessionScore = if (won) currentSessionScore + score else 0

        val challengeMode = challengeConfig.mode
        val baseWin = when {
            isDailyChallenge -> 0
            challengeMode == ChallengeMode.LIMITED_GUESSES -> UserTokens.EARNED_PER_LIMITED_WIN
            else -> UserTokens.EARNED_PER_CLASSIC_WIN
        }
        val comboBonus = if (challengeMode == ChallengeMode.LIMITED_GUESSES) {
            0
        } else {
            gameStatus.maxCombo * UserTokens.COMBO_STEP_TOKENS
        }
        val gamePayout = when {
            gameStatus.mode.isTimed -> 0
            won -> baseWin + comboBonus
            else -> 0
        }
        var tokensEarned = 0
        if (gamePayout > 0) {
            val result = tokenRepository.earnTokens(gamePayout)
            if (result.isSuccess) {
                tokensEarned += gamePayout
            }
        }

        val wallClockEndTime = System.currentTimeMillis()
        val monotonicEndTime = android.os.SystemClock.elapsedRealtime()
        val historyEntry = GameHistoryEntry(
            word = gameStatus.word.value,
            difficulty = gameStatus.word.difficulty,
            won = won,
            score = score,
            sessionScore = newSessionScore,
            guessedLetters = gameStatus.guessedLetters,
            incorrectGuesses = gameStatus.incorrectGuesses,
            elapsedSeconds = (monotonicEndTime - gameStatus.gameStartTime) / 1000,
            playedAt = wallClockEndTime,
            updatedAt = wallClockEndTime,
            category = gameStatus.word.category,
            hintsUsed = hintsUsedThisGame,
            gameMode = gameStatus.mode.name
        )
        gameHistoryRepository.addGameEntry(historyEntry)
        cloudSyncUseCase.syncAll()

        val statistics = statisticsRepository.getStatistics().getOrNull()
        if (statistics != null) {
            val scoreResult = leaderboardRepository.submitScore(
                metric = com.LetterQuest.domain.model.LeaderboardMetric.TOTAL_SCORE,
                value = score.toFloat(),
                gamesPlayed = 1,
                gamesWon = if (won) 1 else 0
            )
            if (scoreResult.isFailure) {
                scope.launch {
                    val authState = authRepository.currentUser.first()
                    val username = (authState as? AuthState.Authenticated)?.username ?: ""
                    val nickname = (authState as? AuthState.Authenticated)?.displayName ?: "Player"
                    cloudSyncUseCase.enqueueLeaderboardScore(
                        metric = com.LetterQuest.domain.model.LeaderboardMetric.TOTAL_SCORE.name,
                        value = score.toFloat(),
                        gamesPlayed = 1,
                        gamesWon = if (won) 1 else 0,
                        username = username,
                        nickname = nickname,
                        avatarId = "avatar_1"
                    )
                }
            }
            achievementUnlocker.evaluateAchievements(
                gameStatus = gameStatus,
                statistics = statistics,
                usedHint = usedHintThisGame,
                isTimedWord = gameStatus.mode.isTimed,
                hintsUsedThisGame = hintsUsedThisGame
            )
        }

        if (isDailyChallenge && won) {
            val completionResult = dailyChallengeRepository.recordCompletion(won = true)
            completionResult.onSuccess {
                val bonusResult = tokenRepository.earnTokens(com.LetterQuest.domain.model.DailyChallenge.COMPLETION_BONUS_TOKENS)
                if (bonusResult.isSuccess) {
                    achievementUnlocker.evaluateDailyAchievements()
                    tokensEarned += com.LetterQuest.domain.model.DailyChallenge.COMPLETION_BONUS_TOKENS
                }
            }
        }

        val finalScore = if (gameStatus.mode.isTimed) gameStatus.score else score
        return GameResultOutcome(
            sessionScore = newSessionScore,
            tokensEarned = tokensEarned,
            finalGameStatus = gameStatus.copy(score = finalScore, gameEndTime = monotonicEndTime)
        )
    }
}

data class GameResultOutcome(
    val sessionScore: Int,
    val tokensEarned: Int,
    val finalGameStatus: GameStatus,
    val error: String? = null
)
