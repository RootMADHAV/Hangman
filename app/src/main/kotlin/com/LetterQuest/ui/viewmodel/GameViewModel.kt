package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val achievementUnlocker: AchievementUnlocker,
    private val gameHistoryRepository: GameHistoryRepository,
    private val tokenRepository: TokenRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val shopRepository: ShopRepository,
    private val cloudSyncUseCase: CloudSyncUseCase,
    private val leaderboardRepository: LeaderboardRepository,
    private val musicPlayer: MusicPlayer,
    private val soundPlayer: SoundPlayer,
    private val preferencesRepository: PreferencesRepository
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

    /** Perks the player has activated for this game. */
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

    /** 1-second ticker driving [GameStatus.timeRemainingSeconds] in timed mode. */
    private var timerJob: Job? = null

    /** Tracks the in-flight [advanceTimedWord] coroutine so it can be cancelled on session expiry. */
    private var advanceTimedWordJob: Job? = null

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

    /**
     * Starts today's daily challenge. The word is fixed for the day, so difficulty and
     * category are taken from the puzzle rather than chosen by the player.
     */
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

    /**
     * Shared start-of-game flow used by every entry point: clears single-use perks,
     * resets per-game UI state, loads a word, and emits the result.
     */
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
                    if (mode.isTimed && (timerJob == null || timerJob?.isActive != true)) {
                        startTimedTimer(GameMode.TIMED_SESSION_SECONDS)
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

    /** Builds a fresh game status for the given [mode]. */
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

    /**
     * Called when the player taps "Continue" on the win-celebration overlay.
     * Loads the next word with the same difficulty and category, keeping the win streak
     * and advancing the Classic level counter.
     */
    fun continueAfterWin() {
        if (_uiState.value.isProcessingAction) return
        updateState { copy(isProcessingAction = true) }

        val finished = _uiState.value.gameStatus ?: return
        val state = _uiState.value
        val challengeMode = state.challengeConfig.mode

        updateState {
            copy(
                totalTokensEarned = state.totalTokensEarned + state.tokensEarnedThisGame
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

    /** Starts a brand-new 60-second timed session with the same difficulty/category. */
    fun restartTimedSession() {
        val status = _uiState.value.gameStatus ?: return
        timerJob?.cancel()
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
    // Timed mode session clock
    // ------------------------------------------------------------------

    private fun startTimedTimer(totalSeconds: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                updateState {
                    copy(gameStatus = gameStatus?.copy(timeRemainingSeconds = remaining))
                }
                delay(1000L)
                // Pause freezes the clock without cancelling the session.
                if (_uiState.value.gameStatus?.isPaused == true) continue
                remaining--
            }
            updateState { copy(gameStatus = gameStatus?.copy(timeRemainingSeconds = 0)) }
            onTimedSessionExpired()
        }
    }

    /**
     * Clock hit zero: finalize any in-flight word, then pay the timed payout —
     * [UserTokens.EARNED_PER_TIMED_WORD] tokens per word solved plus combo bonuses —
     * and surface the summary overlay.
     */
    private fun onTimedSessionExpired() {
        advanceTimedWordJob?.cancel()
        advanceTimedWordJob = null
        val status = _uiState.value.gameStatus ?: return
        if (!status.mode.isTimed) return

        viewModelScope.launch {
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
                } else {
                    android.util.Log.w("GameViewModel", "Failed to earn timed payout: ${result.exceptionOrNull()?.message}")
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
                        gameEndTime = System.currentTimeMillis()
                    ),
                    tokensEarnedThisGame = tokensEarnedThisGame + earned,
                    showingTimedSummary = true
                )
            }

            if (current.state == GameState.PLAYING) {
                recordGameResult(finalized)
            }
        }
    }

    /** Moves straight to the next word inside a running timed session (no overlay). */
    private fun advanceTimedWord(finished: GameStatus) {
        advanceTimedWordJob?.cancel()
        advanceTimedWordJob = viewModelScope.launch {
            val message = if (finished.state == GameState.WON) {
                "✓ Word solved!"
            } else {
                "The word was: ${finished.word.normalizedValue}"
            }
            updateState { copy(hintMessage = message) }

            delay(600L)

            val remaining = _uiState.value.gameStatus?.timeRemainingSeconds ?: 0L
            if (remaining <= 0L) {
                return@launch
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
                    return@launch
                }
                attempts++
            }

            if (nextWord == null) {
                val fallback = wordSelector.selectRandomWord(
                    finished.word.difficulty,
                    _uiState.value.categoryId
                )
                fallback.onSuccess { nextWord = it }
                fallback.onFailure {
                    updateState { copy(error = it.message ?: "Failed to load next word") }
                    return@launch
                }
            }

            val word = nextWord ?: return@launch
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
                    hintMessage = null,
                    hintMessagePersistent = null
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Guessing
    // ------------------------------------------------------------------

    fun guessLetter(letter: Char) {
        val currentState = _uiState.value.gameStatus ?: return
        // Block guesses when game is already over (prevents background tap bug)
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

        // Timed Blitz: the score updates live — +N per correct letter, +bonus when the
        // word completes. Other modes keep score 0 until the win is finalized.
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

    /** Spends [hint]'s cost and applies its effect. */
    fun useHint(hint: HintType) {
        val currentState = _uiState.value.gameStatus ?: return
        if (currentState.isGameOver || currentState.isPaused) return
        if (_uiState.value.isProcessingAction) return  // serialize taps

        // Daily Challenge rules: Skip is forbidden — you must actually solve the
        // daily word — and the Clue and Extra Life power-ups are not offered there.
        if (hint == HintType.SKIP_WORD && _uiState.value.isDailyChallenge) {
            updateState { copy(hintMessage = "Skip is disabled for the daily challenge") }
            return
        }
        if (hint == HintType.EXTRA_LIFE && !currentState.mode.isClassic) {
            updateState { copy(hintMessage = "Extra Life is only available in Classic mode") }
            return
        }

        // The Clue power-up is only offered in Timed Blitz — in Classic mode the clue
        // is already shown up-front, so buying it there would do nothing new.
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
                        updateState { copy(usedHintThisGame = true) }
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
        // Uses the LIVE state (not the captured lambda param) so back-to-back hints
        // see the reveal the previous tap just applied — no repeated reveals.
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

        // Find alphabet letters that are (a) unguessed, and (b) NOT present in the word —
        // these are the "wrong letters" still available as tappable keys.
        val wordLetters = liveStatus.word.normalizedValue.toSet().filter(Char::isLetter)
        val availableWrongLetters = ('A'..'Z')
            .filter { it !in liveStatus.guessedLetters && it !in liveStatus.incorrectGuesses }
            .filter { it !in wordLetters }

        if (availableWrongLetters.isEmpty()) {
            updateState { copy(hintMessage = "No wrong letters left to remove") }
            return
        }

        val removed = availableWrongLetters.shuffled().take(2)

        // Mark them as "incorrectly guessed" — this greys out the tiles and prevents
        // accidental taps — without consuming any of the player's remaining attempts.
        // Refund the player's tokens if we couldn't remove anything useful.
        if (removed.isEmpty()) {
            updateState { copy(hintMessage = "Nothing to remove") }
            return
        }

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
        updateState {
            copy(
                gameStatus = currentState.copy(
                    isPaused = true,
                    pauseStartTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun resumeGame() {
        val currentState = _uiState.value.gameStatus ?: return
        if (!currentState.isPaused || currentState.pauseStartTime == null) return
        val pauseDuration = System.currentTimeMillis() - currentState.pauseStartTime
        updateState {
            copy(
                gameStatus = currentState.copy(
                    isPaused = false,
                    pauseStartTime = null,
                    totalPausedMillis = currentState.totalPausedMillis + pauseDuration
                )
            )
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
            recordGameResult(newGameStatus)
            advanceTimedWord(newGameStatus)
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
            recordGameResult(withStars)
        } else {
            recordGameResult(newGameStatus)
            viewModelScope.launch { playSoundIfEnabled { soundPlayer.playGameOverSound() } }
        }

        viewModelScope.launch {
            shopRepository.clearActivatedPerks()
        }
    }

    /** Called when the player taps "Go Home" on the daily-win overlay. */
    fun dismissDailyWinCelebration() {
        if (_uiState.value.isProcessingAction) return
        updateState { copy(showingDailyWinCelebration = false) }
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

    private fun recordGameResult(gameStatus: GameStatus) {
        viewModelScope.launch {
            try {
                val perks = gameStatus.perks
                val scoreMultiplier = if (ShopItem.SCORE_BOOST in perks) {
                    ShopItem.SCORE_BOOST_MULTIPLIER
                } else {
                    1f
                } * _uiState.value.challengeConfig.scoreMultiplier
                val score = ScoreCalculator.calculateScore(gameStatus, scoreMultiplier)
                val won = gameStatus.state == GameState.WON
                statisticsRepository.recordGameResult(won, score)

                val currentSessionScore = _uiState.value.sessionScore
                val newSessionScore = if (won) currentSessionScore + score else currentSessionScore
                updateState { copy(sessionScore = newSessionScore) }

                val challengeMode = _uiState.value.challengeConfig.mode
                val baseWin = when {
                    _uiState.value.isDailyChallenge -> 0
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
                if (gamePayout > 0) {
                    val result = tokenRepository.earnTokens(gamePayout)
                    if (result.isSuccess) {
                        updateState { copy(tokensEarnedThisGame = tokensEarnedThisGame + gamePayout) }
                    } else {
                        android.util.Log.w("GameViewModel", "Failed to earn game payout: ${result.exceptionOrNull()?.message}")
                    }
                }

                val gameEndTime = System.currentTimeMillis()
                val historyEntry = GameHistoryEntry(
                    word = gameStatus.word.value,
                    difficulty = gameStatus.word.difficulty,
                    won = won,
                    score = score,
                    sessionScore = newSessionScore,
                    guessedLetters = gameStatus.guessedLetters,
                    incorrectGuesses = gameStatus.incorrectGuesses,
                    elapsedSeconds = (gameEndTime - gameStatus.gameStartTime) / 1000,
                    playedAt = gameEndTime,
                    updatedAt = gameEndTime,
                    category = gameStatus.word.category
                )
                gameHistoryRepository.addGameEntry(historyEntry)
                viewModelScope.launch { cloudSyncUseCase.syncAll() }

                val statistics = statisticsRepository.getStatistics().getOrNull()
                if (statistics != null) {
                    leaderboardRepository.submitScore(
                        metric = LeaderboardMetric.TOTAL_SCORE,
                        value = score.toFloat(),
                        gamesPlayed = statistics.gamesPlayed,
                        gamesWon = statistics.gamesWon
                    )
                    achievementUnlocker.evaluateAchievements(
                        gameStatus = gameStatus,
                        statistics = statistics,
                        usedHint = _uiState.value.usedHintThisGame,
                        isTimedWord = gameStatus.mode.isTimed
                    )
                }

                if (!won) {
                    updateState { copy(sessionScore = 0) }
                }

                if (_uiState.value.isDailyChallenge && won) {
                    val completionResult = dailyChallengeRepository.recordCompletion(won = true)
                    completionResult.onSuccess {
                        val bonusResult = tokenRepository.earnTokens(DailyChallenge.COMPLETION_BONUS_TOKENS)
                        if (bonusResult.isSuccess) {
                            achievementUnlocker.evaluateDailyAchievements()
                            updateState {
                                copy(
                                    tokensEarnedThisGame = tokensEarnedThisGame +
                                        DailyChallenge.COMPLETION_BONUS_TOKENS
                                )
                            }
                        } else {
                            android.util.Log.w("GameViewModel", "Failed to earn daily completion bonus: ${bonusResult.exceptionOrNull()?.message}")
                        }
                    }.onFailure {
                        updateState { copy(error = it.message ?: "Failed to record daily challenge completion") }
                    }
                }

                val finalScore = if (gameStatus.mode.isTimed) gameStatus.score else score
                updateState { copy(gameStatus = gameStatus.copy(score = finalScore, gameEndTime = gameEndTime)) }
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to save game result") }
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
        timerJob?.cancel()
        timerJob = null
        _uiState.value = GameUIState()
        musicPlayer.resume()
    }

    /** Retry feature removed — players move to a new word after a loss. */
    fun retryCurrentWord() = Unit

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    /** Atomic state update helper — avoids the read-then-write race of
     *  `_uiState.value = _uiState.value.copy(...)`. */
    private inline fun updateState(transform: GameUIState.() -> GameUIState) {
        _uiState.update(transform)
    }

    companion object {
        /**
         * Challenge mode chosen on the setup screen, consumed by the next gameplay screen.
         *
         * Because each navigation destination receives its own ViewModel instance and
         * the mode cannot be smuggled through the (shared, frozen) navigation routes,
         * the selection is parked here and claimed by the gameplay screen when it
         * initializes its game. Defaults back to [ChallengeMode.CLASSIC] once consumed.
         */
        @Volatile
        private var pendingChallengeMode: ChallengeMode = ChallengeMode.CLASSIC

        /** Call before navigating toward the gameplay screen to pick the next game's challenge mode. */
        fun selectChallengeModeForNextGame(mode: ChallengeMode) {
            pendingChallengeMode = mode
        }

        /** Returns the queued mode and resets the slot to [ChallengeMode.CLASSIC]. */
        fun consumePendingChallengeMode(): ChallengeMode {
            val mode = pendingChallengeMode
            pendingChallengeMode = ChallengeMode.CLASSIC
            return mode
        }
    }
}
