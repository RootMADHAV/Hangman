package com.LetterQuest.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.ChallengeMode
import com.LetterQuest.domain.model.ChallengeModeConfig
import com.LetterQuest.domain.model.DailyChallenge
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.GameMode
import com.LetterQuest.domain.model.GameState
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.GuessResult
import com.LetterQuest.domain.model.HintType
import com.LetterQuest.domain.model.ShopItem
import com.LetterQuest.domain.model.StarRating
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.domain.model.Word
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.domain.model.LeaderboardMetric
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.PreferencesRepository
import com.LetterQuest.domain.repository.ShopRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.domain.repository.TokenRepository
import com.LetterQuest.domain.usecase.AchievementUnlocker
import com.LetterQuest.domain.usecase.CloudSyncUseCase
import com.LetterQuest.domain.usecase.GameplayConfig
import com.LetterQuest.domain.usecase.GuessingEngine
import com.LetterQuest.domain.usecase.MusicPlayer
import com.LetterQuest.domain.usecase.ScoreCalculator
import com.LetterQuest.domain.usecase.SoundPlayer
import com.LetterQuest.domain.usecase.WordSelector
import com.LetterQuest.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUIState(
    val gameStatus: GameStatus? = null,
    val guessResult: GuessResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastGuessedLetter: Char? = null,
    val categoryId: String = WordCategory.ALL_CATEGORIES_ID,
    val tokensEarnedThisGame: Int = 0,
    val hintMessage: String? = null,
    val hintMessagePersistent: String? = null,
    val usedHintThisGame: Boolean = false,
    val hintsUsedThisGame: Int = 0,
    val isDailyChallenge: Boolean = false,
    val winStreak: Int = 0,
    val showingWinCelebration: Boolean = false,
    val showingDailyWinCelebration: Boolean = false,
    val totalTokensEarned: Int = 0,
    val isProcessingAction: Boolean = false,
    val timedWordsSolved: Int = 0,
    val showingTimedSummary: Boolean = false,
    val challengeConfig: ChallengeModeConfig = ChallengeModeConfig(ChallengeMode.CLASSIC),
    val categoryWordsCompleted: Int = 0,
    val sessionScore: Int = 0,
    val timedWordMaxCombos: List<Int> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordSelector: WordSelector,
    private val statisticsRepository: StatisticsRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val tokenRepository: TokenRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val shopRepository: ShopRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val musicPlayer: MusicPlayer,
    private val soundPlayer: SoundPlayer,
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: com.LetterQuest.domain.repository.AuthRepository,
    private val gameTimer: GameTimer,
    private val gameResultHandler: GameResultHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUIState())
    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    val tokenBalance: StateFlow<Int> = tokenRepository.observeTokens()
        .map { it.balance }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UserTokens.STARTING_BALANCE
        )

    val ownedPerks: StateFlow<Set<ShopItem>> = shopRepository.observeOwnedItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptySet()
        )

    private val soundEnabled: StateFlow<Boolean> = preferencesRepository
        .observePreferences()
        .map { it.soundEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** [hint]'s price given the perks the player has active and the current mode. */
    fun effectiveHintCost(hint: HintType, perks: Set<ShopItem>): Int {
        var cost = hint.cost.toFloat()
        if (_uiState.value.gameStatus?.mode?.isTimed == true) {
            cost *= GameMode.TIMED_HINT_COST_MULTIPLIER
        }
        if (ShopItem.HINT_DISCOUNT in perks) {
            cost *= ShopItem.HINT_DISCOUNT_MULTIPLIER
        }
        return maxOf(GameMode.MIN_HINT_COST, Math.round(cost))
    }

    private suspend fun playSoundIfEnabled(play: suspend () -> Unit) {
        if (soundEnabled.value) {
            play()
        }
    }

    // ------------------------------------------------------------------
    // Game lifecycle
    // ------------------------------------------------------------------

    fun initializeGame(
        difficulty: Difficulty,
        categoryId: String = WordCategory.ALL_CATEGORIES_ID,
        challengeMode: ChallengeMode = ChallengeMode.CLASSIC
    ) {
        musicPlayer.pause()
        val excludedWord = _uiState.value.gameStatus?.word?.normalizedValue
        val challengeConfig = ChallengeModeConfig(
            mode = challengeMode,
            maxGuesses = when (challengeMode) {
                ChallengeMode.LIMITED_GUESSES -> 3
                else -> null
            },
            selectedCategory = if (challengeMode == ChallengeMode.CATEGORY_CHALLENGE) categoryId else null,
            scoreMultiplier = when (challengeMode) {
                ChallengeMode.LIMITED_GUESSES -> 1.5f
                ChallengeMode.CATEGORY_CHALLENGE -> 1.3f
                else -> 1f
            }
        )
        startGame(
            categoryId = categoryId,
            isDailyChallenge = false,
            challengeMode = challengeMode,
            challengeConfig = challengeConfig,
            errorFallback = "Failed to load word",
            wordSource = {
                when {
                    challengeMode == ChallengeMode.CATEGORY_CHALLENGE -> {
                        val pool = wordSelector.getWordsForCategory(difficulty, categoryId)
                            .getOrNull().orEmpty()
                            .filter { it.normalizedValue != excludedWord }
                            .shuffled()
                        if (pool.isNotEmpty()) {
                            Result.success(pool.first())
                        } else {
                            Result.failure(IllegalArgumentException("No words available for this category challenge"))
                        }
                    }
                    challengeMode == ChallengeMode.LIMITED_GUESSES ->
                        wordSelector.selectRandomWordExcluding(difficulty, categoryId, excludedWord ?: "")
                    excludedWord != null ->
                        wordSelector.selectRandomWordExcluding(difficulty, categoryId, excludedWord)
                    else ->
                        wordSelector.selectRandomWord(difficulty, categoryId)
                }
            }
        )
    }

    fun initializeDailyChallenge() {
        musicPlayer.pause()
        viewModelScope.launch {
            dailyChallengeRepository.recordAttempt()
            val challengeResult = dailyChallengeRepository.getTodaysChallenge()
            startGame(
                categoryId = challengeResult.getOrNull()?.word?.category
                    ?: WordCategory.ALL_CATEGORIES_ID,
                isDailyChallenge = true,
                errorFallback = "Failed to load daily challenge",
                wordSource = { challengeResult.map { it.word } }
            )
        }
    }

    private fun startGame(
        categoryId: String,
        isDailyChallenge: Boolean,
        challengeMode: ChallengeMode = ChallengeMode.CLASSIC,
        challengeConfig: ChallengeModeConfig = ChallengeModeConfig(ChallengeMode.CLASSIC),
        levelIndex: Int = 1,
        errorFallback: String = "Failed to load word",
        wordSource: suspend () -> Result<Word>
    ) {
        viewModelScope.launch {
            val perks = shopRepository.getOwnedItems().getOrNull().orEmpty()
            updateState {
                copy(
                    isLoading = true,
                    error = null,
                    categoryId = categoryId,
                    tokensEarnedThisGame = 0,
                    hintMessage = null,
                    usedHintThisGame = false,
                    hintsUsedThisGame = 0,
                    isDailyChallenge = isDailyChallenge,
                    showingWinCelebration = false,
                    showingTimedSummary = false,
                    challengeConfig = challengeConfig,
                    timedWordMaxCombos = emptyList()
                )
            }

            wordSource().fold(
                onSuccess = { word ->
                    val mode = if (challengeMode == ChallengeMode.TIMED) GameMode.TIMED else GameMode.CLASSIC
                    val status = newGameStatus(
                        word = word,
                        mode = mode,
                        levelIndex = levelIndex,
                        challengeMode = challengeMode,
                        challengeConfig = challengeConfig,
                        perks = perks
                    )
                    updateState { copy(gameStatus = status, isLoading = false, isProcessingAction = false) }
                    if (mode.isTimed) {
                        gameTimer.start(
                            totalSeconds = GameMode.TIMED_SESSION_SECONDS,
                            scope = this,
                            onTick = { remaining ->
                                updateState { copy(gameStatus = gameStatus?.copy(timeRemainingSeconds = remaining)) }
                            }
                        ) {
                            onTimedSessionExpired()
                        }
                    }
                },
                onFailure = { error ->
                    updateState {
                        copy(isLoading = false, error = error.message ?: errorFallback, isProcessingAction = false)
                    }
                }
            )
        }
    }

    private suspend fun newGameStatus(
        word: Word,
        mode: GameMode = GameMode.CLASSIC,
        levelIndex: Int = 1,
        timeRemainingSeconds: Long? = null,
        timedSolved: Int = 0,
        keepScore: Int = 0,
        perks: Set<ShopItem> = emptySet(),
        challengeMode: ChallengeMode = ChallengeMode.CLASSIC,
        challengeConfig: ChallengeModeConfig = ChallengeModeConfig(ChallengeMode.CLASSIC)
    ): GameStatus {
        val remainingAttempts = when (challengeMode) {
            ChallengeMode.LIMITED_GUESSES -> challengeConfig.maxGuesses ?: word.difficulty.maxAttempts
            else -> word.difficulty.maxAttempts
        }
        val config = mode.config()
        return GameStatus(
            word = word,
            remainingAttempts = remainingAttempts,
            mode = mode,
            levelIndex = levelIndex,
            showClue = config.showClueUpFront,
            timeRemainingSeconds = if (mode.isTimed) {
                timeRemainingSeconds ?: GameMode.TIMED_SESSION_SECONDS
            } else {
                null
            },
            timedWordsSolved = timedSolved,
            score = keepScore,
            perks = perks
        )
    }

    fun continueAfterWin() {
        if (_uiState.value.isProcessingAction) return
        updateState { copy(isProcessingAction = true) }

        val finished = _uiState.value.gameStatus ?: return
        val state = _uiState.value
        val challengeMode = state.challengeConfig.mode

        updateState {
            copy(
                totalTokensEarned = totalTokensEarned + tokensEarnedThisGame
            )
        }

        if (challengeMode == ChallengeMode.CATEGORY_CHALLENGE) {
            updateState { copy(categoryWordsCompleted = categoryWordsCompleted + 1) }
        }

        startGame(
            categoryId = state.categoryId,
            isDailyChallenge = false,
            challengeMode = challengeMode,
            challengeConfig = state.challengeConfig,
            levelIndex = finished.levelIndex + 1,
            errorFallback = "Failed to load next word",
            wordSource = {
                when (challengeMode) {
                    ChallengeMode.CATEGORY_CHALLENGE -> {
                        wordSelector.selectRandomWordExcluding(
                            finished.word.difficulty,
                            state.categoryId,
                            finished.word.normalizedValue
                        )
                    }
                    else ->
                        wordSelector.selectRandomWordExcluding(
                            finished.word.difficulty,
                            state.categoryId,
                            finished.word.normalizedValue
                        )
                }
            }
        )
    }

    fun restartTimedSession() {
        val status = _uiState.value.gameStatus ?: return
        gameTimer.cancel()
        updateState {
            copy(
                totalTokensEarned = totalTokensEarned + tokensEarnedThisGame,
                timedWordsSolved = 0,
                showingTimedSummary = false,
                sessionScore = 0,
                timedWordMaxCombos = emptyList()
            )
        }
        viewModelScope.launch {
            shopRepository.clearActivatedPerks()
        }
        initializeGame(
            status.word.difficulty,
            _uiState.value.categoryId,
            _uiState.value.challengeConfig.mode
        )
    }

    // ------------------------------------------------------------------
    // Guessing
    // ------------------------------------------------------------------

    fun guessLetter(letter: Char) {
        val currentState = _uiState.value.gameStatus ?: return
        if (currentState.isGameOver || currentState.isPaused) return

        if (!GuessingEngine.validateLetter(letter)) {
            updateState { copy(guessResult = GuessResult.Invalid) }
            return
        }

        val (newGameStatus, result) = GuessingEngine.processGuess(currentState, letter)

        val newCombo = when (result) {
            is GuessResult.Correct -> currentState.currentCombo + 1
            is GuessResult.Incorrect -> 0
            else -> currentState.currentCombo
        }
        val newMaxCombo = maxOf(currentState.maxCombo, newCombo)

        val scoredStatus = if (newGameStatus.mode.isTimed && result == GuessResult.Correct) {
            newGameStatus.copy(
                score = newGameStatus.score + GameMode.TIMED_POINTS_PER_CORRECT_LETTER +
                    if (newGameStatus.state == GameState.WON) GameMode.TIMED_POINTS_PER_WORD_SOLVED else 0,
                currentCombo = newCombo,
                maxCombo = newMaxCombo
            )
        } else {
            newGameStatus.copy(
                currentCombo = newCombo,
                maxCombo = newMaxCombo
            )
        }

        updateState {
            copy(
                gameStatus = scoredStatus,
                guessResult = result,
                lastGuessedLetter = letter,
                hintMessage = hintMessagePersistent
            )
        }

        if (result == GuessResult.Correct) {
            awardTokens(1)
            viewModelScope.launch { playSoundIfEnabled { soundPlayer.playCorrectGuessSound() } }
        } else if (result == GuessResult.Incorrect) {
            viewModelScope.launch { playSoundIfEnabled { soundPlayer.playIncorrectGuessSound() } }
            val remaining = scoredStatus.remainingAttempts
            if (remaining <= 2 && remaining > 0 && !scoredStatus.isGameOver) {
                viewModelScope.launch { playSoundIfEnabled { soundPlayer.playLowLivesSound() } }
            }
        }

        if (scoredStatus.isGameOver) {
            handleGameOver(scoredStatus)
        }
    }

    fun clearGuessResult() {
        updateState { copy(guessResult = null) }
    }

    // ------------------------------------------------------------------
    // Hints
    // ------------------------------------------------------------------

    fun useHint(hint: HintType) {
        val currentState = _uiState.value.gameStatus ?: return
        if (currentState.isGameOver || currentState.isPaused) return
        if (_uiState.value.isProcessingAction) return

        if (hint == HintType.SKIP_WORD && _uiState.value.isDailyChallenge) {
            updateState { copy(hintMessage = "Skip is disabled for the daily challenge") }
            return
        }
        if (hint == HintType.EXTRA_LIFE && !currentState.mode.isClassic) {
            updateState { copy(hintMessage = "Extra Life is only available in Classic mode") }
            return
        }
        if (hint == HintType.CLUE && !currentState.mode.isTimed) {
            updateState { copy(hintMessage = "Clue is already shown when available") }
            return
        }

        viewModelScope.launch {
            updateState { copy(isProcessingAction = true) }
            try {
                val perks = currentState.perks
                val cost = effectiveHintCost(hint, perks)

                tokenRepository.spendTokens(cost)
                    .onSuccess {
                        updateState {
                            copy(
                                usedHintThisGame = true,
                                hintsUsedThisGame = hintsUsedThisGame + 1
                            )
                        }
                        when (hint) {
                            HintType.CLUE -> applyShowHint(currentState)
                            HintType.REVEAL_LETTER -> applyRevealLetter(currentState)
                            HintType.REMOVE_WRONG_LETTERS -> applyRemoveWrongLetters(currentState)
                            HintType.SKIP_WORD -> applySkipWord(currentState)
                            HintType.EXTRA_LIFE -> applyExtraLife(currentState)
                        }
                        viewModelScope.launch { playSoundIfEnabled { soundPlayer.playHintSound() } }
                    }
                    .onFailure {
                        updateState { copy(hintMessage = "Not enough tokens for ${hint.displayName}") }
                    }
            } finally {
                updateState { copy(isProcessingAction = false) }
            }
        }
    }

    private fun applyShowHint(currentState: GameStatus) {
        val liveStatus = _uiState.value.gameStatus ?: currentState
        if (liveStatus.showClue) {
            updateState { copy(hintMessage = "Clue is already shown") }
            return
        }
        val hint = liveStatus.word.hint
        updateState {
            copy(
                gameStatus = liveStatus.copy(showClue = true),
                hintMessage = if (!hint.isNullOrBlank()) "🔍 $hint" else "No clue available for this word",
                hintMessagePersistent = if (!hint.isNullOrBlank()) "🔍 $hint" else null
            )
        }
    }

    private fun applyRevealLetter(currentState: GameStatus) {
        val liveStatus = _uiState.value.gameStatus ?: currentState
        val hidden = (liveStatus.word.normalizedValue.toSet() - liveStatus.guessedLetters)
            .filter(Char::isLetter)

        if (hidden.isEmpty()) {
            updateState { copy(hintMessage = "Nothing left to reveal") }
            return
        }

        val revealed = hidden.random()
        val updated = liveStatus.copy(guessedLetters = liveStatus.guessedLetters + revealed)
        val finished = if (updated.isWordComplete()) updated.copy(state = GameState.WON) else updated

        updateState {
            copy(
                gameStatus = finished,
                hintMessage = "Revealed the letter $revealed",
                hintMessagePersistent = "Revealed the letter $revealed"
            )
        }

        if (finished.isGameOver) {
            handleGameOver(finished)
        }
    }

    private fun applyRemoveWrongLetters(currentState: GameStatus) {
        val liveStatus = _uiState.value.gameStatus ?: currentState
        val wordLetters = liveStatus.word.normalizedValue.toSet().filter(Char::isLetter)
        val availableWrongLetters = ('A'..'Z')
            .filter { it !in liveStatus.guessedLetters && it !in liveStatus.incorrectGuesses }
            .filter { it !in wordLetters }

        if (availableWrongLetters.isEmpty()) {
            updateState { copy(hintMessage = "No wrong letters left to remove") }
            return
        }

        val removed = availableWrongLetters.shuffled().take(2)
        val updated = liveStatus.copy(
            incorrectGuesses = liveStatus.incorrectGuesses + removed.toSet()
        )

        updateState {
            copy(
                gameStatus = updated,
                hintMessage = "Removed: ${removed.joinToString(", ")}",
                hintMessagePersistent = "Removed: ${removed.joinToString(", ")}"
            )
        }
    }

    private fun applyExtraLife(currentState: GameStatus) {
        val liveStatus = _uiState.value.gameStatus ?: currentState
        val updated = liveStatus.copy(
            remainingAttempts = liveStatus.remainingAttempts + 1
        )
        updateState {
            copy(
                gameStatus = updated,
                hintMessage = "❤️ Extra Life! +1 attempt",
                hintMessagePersistent = "❤️ Extra Life! +1 attempt"
            )
        }
    }

    private suspend fun applySkipWord(currentState: GameStatus) {
        wordSelector.selectRandomWordExcluding(
            currentState.word.difficulty,
            _uiState.value.categoryId,
            currentState.word.normalizedValue
        ).onSuccess { word ->
            updateState {
                copy(
                    gameStatus = newGameStatus(
                        word,
                        mode = currentState.mode,
                        levelIndex = currentState.levelIndex,
                        timeRemainingSeconds = currentState.timeRemainingSeconds,
                        timedSolved = currentState.timedWordsSolved,
                        keepScore = currentState.score
                    ),
                    guessResult = null,
                    lastGuessedLetter = null,
                    hintMessage = "Swapped in a new word",
                    hintMessagePersistent = "Swapped in a new word"
                )
            }
        }.onFailure { error ->
            updateState { copy(hintMessage = error.message ?: "Could not find another word") }
        }
    }

    fun clearHintMessage() {
        updateState { copy(hintMessage = null) }
    }

    // ------------------------------------------------------------------
    // Pause / resume
    // ------------------------------------------------------------------

    fun pauseGame() {
        val currentState = _uiState.value.gameStatus ?: return
        if (currentState.isPaused || currentState.isGameOver) return
        gameTimer.cancel()
        updateState {
            copy(
                gameStatus = currentState.copy(
                    isPaused = true,
                    pauseStartTime = android.os.SystemClock.elapsedRealtime()
                )
            )
        }
    }

    fun resumeGame() {
        val currentState = _uiState.value.gameStatus ?: return
        if (!currentState.isPaused || currentState.pauseStartTime == null) return
        val pauseDuration = android.os.SystemClock.elapsedRealtime() - currentState.pauseStartTime
        updateState {
            copy(
                gameStatus = currentState.copy(
                    isPaused = false,
                    pauseStartTime = null,
                    totalPausedMillis = currentState.totalPausedMillis + pauseDuration
                )
            )
        }
        if (currentState.mode.isTimed) {
            val remaining = _uiState.value.gameStatus?.timeRemainingSeconds ?: 0L
            if (remaining > 0L) {
                gameTimer.start(
                    totalSeconds = remaining,
                    scope = viewModelScope,
                    onTick = { tick ->
                        updateState { copy(gameStatus = gameStatus?.copy(timeRemainingSeconds = tick)) }
                    }
                ) {
                    onTimedSessionExpired()
                }
            }
            if (currentState.state == GameState.WON && !_uiState.value.showingTimedSummary) {
                advanceTimedWord(currentState)
            }
        }
    }

    // ------------------------------------------------------------------
    // End of game
    // ------------------------------------------------------------------

    private fun handleGameOver(newGameStatus: GameStatus) {
        if (newGameStatus.mode.isTimed) {
            if (newGameStatus.state == GameState.WON) {
                updateState {
                    copy(
                        winStreak = winStreak + 1,
                        timedWordsSolved = timedWordsSolved + 1,
                        timedWordMaxCombos = timedWordMaxCombos + newGameStatus.maxCombo
                    )
                }
            }
            viewModelScope.launch {
                recordGameResult(newGameStatus)
                advanceTimedWord(newGameStatus)
            }
            return
        }

        if (newGameStatus.state == GameState.WON) {
            val stars = StarRating.forElapsedSeconds(newGameStatus.elapsedSeconds)
            val withStars = newGameStatus.copy(approvedStarRating = stars)
            if (_uiState.value.isDailyChallenge) {
                updateState { copy(gameStatus = withStars, showingDailyWinCelebration = true) }
            } else {
                updateState {
                    copy(
                        gameStatus = withStars,
                        winStreak = winStreak + 1,
                        showingWinCelebration = true
                    )
                }
            }
            viewModelScope.launch { recordGameResult(withStars) }
        } else {
            viewModelScope.launch { recordGameResult(newGameStatus) }
            viewModelScope.launch { playSoundIfEnabled { soundPlayer.playGameOverSound() } }
        }

        viewModelScope.launch {
            shopRepository.clearActivatedPerks()
        }
    }

    suspend fun recordGameResult(gameStatus: GameStatus) {
        val outcome = gameResultHandler.process(
            scope = viewModelScope,
            gameStatus = gameStatus,
            currentSessionScore = _uiState.value.sessionScore,
            challengeConfig = _uiState.value.challengeConfig,
            isDailyChallenge = _uiState.value.isDailyChallenge,
            hintsUsedThisGame = _uiState.value.hintsUsedThisGame,
            usedHintThisGame = _uiState.value.usedHintThisGame
        )
        outcome.error?.let { updateState { copy(error = it) } }
        updateState {
            copy(
                sessionScore = outcome.sessionScore,
                tokensEarnedThisGame = tokensEarnedThisGame + outcome.tokensEarned,
                gameStatus = outcome.finalGameStatus
            )
        }
    }

    private suspend fun onTimedSessionExpired() {
        gameTimer.cancelAdvance()
        val status = _uiState.value.gameStatus ?: return
        if (!status.mode.isTimed) return

        val solved = _uiState.value.timedWordsSolved
        val combos = _uiState.value.timedWordMaxCombos
        val basePayout = solved * UserTokens.EARNED_PER_TIMED_WORD
        val comboPayout = combos.sumOf { it * UserTokens.COMBO_STEP_TOKENS }
        val payout = basePayout + comboPayout
        var earned = 0
        if (payout > 0) {
            val result = tokenRepository.earnTokens(payout)
            if (result.isSuccess) {
                earned = payout
            }
        }

        val current = _uiState.value.gameStatus ?: status
        val finalized = if (current.state == GameState.PLAYING) {
            current.copy(state = GameState.LOST)
        } else {
            current
        }

        updateState {
            copy(
                gameStatus = finalized.copy(
                    timeRemainingSeconds = 0,
                    gameEndTime = android.os.SystemClock.elapsedRealtime()
                ),
                tokensEarnedThisGame = tokensEarnedThisGame + earned,
                showingTimedSummary = true
            )
        }

        if (current.state == GameState.PLAYING) {
            recordGameResult(finalized)
        }
    }

    private fun advanceTimedWord(finished: GameStatus) {
        viewModelScope.launch {
            val message = if (finished.state == GameState.WON) {
                "✓ Word solved!"
            } else {
                "The word was: ${finished.word.normalizedValue}"
            }
            updateState { copy(hintMessage = message) }

            gameTimer.advanceAfterDelay(this) {
                val remaining = _uiState.value.gameStatus?.timeRemainingSeconds ?: 0L
                if (remaining <= 0L || _uiState.value.showingTimedSummary) {
                    return@advanceAfterDelay
                }
                val modeledWords = _uiState.value.timedWordsSolved
                val challengeMode = _uiState.value.challengeConfig.mode

                var nextWord: Word? = null
                var attempts = 0
                while (nextWord == null && attempts < 3) {
                    val result = wordSelector.selectRandomWordExcluding(
                        finished.word.difficulty,
                        _uiState.value.categoryId,
                        finished.word.normalizedValue
                    )
                    result.onSuccess { word ->
                        if (word.normalizedValue != finished.word.normalizedValue) {
                            nextWord = word
                        }
                    }.onFailure {
                        updateState { copy(error = it.message ?: "Failed to load next word") }
                        return@advanceAfterDelay
                    }
                    attempts++
                }

                if (nextWord == null) {
                    val fallback = wordSelector.selectRandomWordExcluding(
                        finished.word.difficulty,
                        _uiState.value.categoryId,
                        finished.word.normalizedValue
                    )
                    fallback.onSuccess { nextWord = it }
                    fallback.onFailure {
                        updateState { copy(error = it.message ?: "Failed to load next word") }
                        return@advanceAfterDelay
                    }
                }

                val word = nextWord ?: return@advanceAfterDelay
                val status = newGameStatus(
                    word,
                    mode = GameMode.TIMED,
                    timeRemainingSeconds = _uiState.value.gameStatus?.timeRemainingSeconds,
                    timedSolved = modeledWords,
                    keepScore = finished.score,
                    challengeMode = challengeMode,
                    challengeConfig = _uiState.value.challengeConfig
                )
                updateState {
                    copy(
                        gameStatus = status,
                        guessResult = null,
                        lastGuessedLetter = null,
                        usedHintThisGame = false,
                        hintsUsedThisGame = 0,
                        hintMessage = null,
                        hintMessagePersistent = null
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Daily challenge ad retry
    // ------------------------------------------------------------------

    val dailyAdRetryAvailable: StateFlow<Boolean> = dailyChallengeRepository
        .hasAdRetryAvailableFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    fun useDailyAdRetry() {
        viewModelScope.launch {
            dailyChallengeRepository.markAdRetryUsed()
        }
    }

    // ------------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------------

    fun resetGame() {
        gameTimer.cancel()
        _uiState.value = GameUIState()
        musicPlayer.resume()
    }

    fun dismissDailyWinCelebration() {
        if (_uiState.value.isProcessingAction) return
        updateState { copy(showingDailyWinCelebration = false) }
    }

    fun retryCurrentWord() = Unit

    override fun onCleared() {
        gameTimer.onCleared()
        musicPlayer.resume()
        super.onCleared()
    }

    private inline fun updateState(transform: GameUIState.() -> GameUIState) {
        _uiState.update(transform)
    }

    private fun awardTokens(amount: Int) {
        viewModelScope.launch {
            tokenRepository.earnTokens(amount)
                .onSuccess {
                    updateState { copy(tokensEarnedThisGame = tokensEarnedThisGame + amount) }
                }
                .onFailure {
                    android.util.Log.w("GameViewModel", "Failed to award $amount tokens: ${it.message}")
                }
        }
    }

    companion object {
        @Volatile
        private var pendingChallengeMode: ChallengeMode = ChallengeMode.CLASSIC

        fun selectChallengeModeForNextGame(mode: ChallengeMode) {
            pendingChallengeMode = mode
        }

        fun consumePendingChallengeMode(): ChallengeMode {
            val mode = pendingChallengeMode
            pendingChallengeMode = ChallengeMode.CLASSIC
            return mode
        }
    }
}
