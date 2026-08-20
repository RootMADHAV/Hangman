package com.LetterQuest.ui.viewmodel

import com.LetterQuest.domain.usecase.GameplayConfig
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.usecase.AchievementUnlocker
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.LeaderboardRepository
import com.LetterQuest.domain.usecase.CloudSyncUseCase
import com.LetterQuest.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the timed-mode session clock and word-advancement job.
 *
 * Exposes its own [MutableStateFlow] for remaining seconds so GameViewModel
 * can observe it without holding timer coroutines directly.
 */
class GameTimer @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val statisticsRepository: StatisticsRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val achievementUnlocker: AchievementUnlocker,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val cloudSyncUseCase: CloudSyncUseCase,
    private val shopRepository: ShopRepository
) {
    private var timerJob: Job? = null
    private var advanceJob: Job? = null

    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds: StateFlow<Long?> = _remainingSeconds.asStateFlow()

    fun start(
        totalSeconds: Long,
        scope: CoroutineScope,
        onTick: (Long) -> Unit,
        onExpired: suspend () -> Unit
    ) {
        timerJob?.cancel()
        _remainingSeconds.value = totalSeconds
        timerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                onTick(remaining)
                _remainingSeconds.value = remaining
                delay(GameplayConfig.TIMER_TICK_MS)
                remaining--
            }
            onTick(0)
            _remainingSeconds.value = 0
            onExpired()
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
    }

    fun advanceAfterDelay(
        scope: CoroutineScope,
        onAdvance: suspend () -> Unit
    ) {
        advanceJob?.cancel()
        advanceJob = scope.launch {
            delay(GameplayConfig.ADVANCE_TIMED_WORD_DELAY_MS)
            onAdvance()
        }
    }

    fun cancelAdvance() {
        advanceJob?.cancel()
        advanceJob = null
    }

    fun onCleared() {
        timerJob?.cancel()
        advanceJob?.cancel()
        timerJob = null
        advanceJob = null
        _remainingSeconds.value = null
    }
}
