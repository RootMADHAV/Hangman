@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.ChallengeMode
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameMode
import com.LetterQuest.domain.model.GameState
import com.LetterQuest.domain.model.GameStatus
import com.LetterQuest.domain.model.GuessResult
import com.LetterQuest.domain.model.HintType
import com.LetterQuest.domain.model.RewardType
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.ui.components.AchievementNotification
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.AdViewModel
import com.LetterQuest.ui.viewmodel.GameViewModel
import com.LetterQuest.ui.viewmodel.NotificationViewModel
import com.LetterQuest.ui.viewmodel.RewardedAdViewModel
import com.LetterQuest.ui.viewmodel.SoundViewModel
import com.LetterQuest.util.findActivity

@Composable
fun GameplayScreen(
    navController: NavHostController,
    difficultyString: String = "MEDIUM",
    categoryId: String = WordCategory.ALL_CATEGORIES_ID,
    isDailyChallenge: Boolean = false,
    gameViewModel: GameViewModel = hiltViewModel(),
    soundViewModel: SoundViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    rewardedAdViewModel: RewardedAdViewModel = hiltViewModel()
) {
    val uiState = gameViewModel.uiState.collectAsState()
    val tokenBalance = gameViewModel.tokenBalance.collectAsState().value
    val ownedPerks = gameViewModel.ownedPerks.collectAsState().value
    val notificationState = notificationViewModel.notificationState.collectAsState().value
    val isHintsEnabled = soundViewModel.isHintsEnabled.collectAsState().value

    // Local dialog state: shown when the player taps a hint they can't afford.
    var showHintAdDialog by remember { mutableStateOf(false) }
    var pendingHintType by remember { mutableStateOf<HintType?>(null) }
    val adsRemoved by rewardedAdViewModel.adsRemoved.collectAsState()
    val context = LocalContext.current

    // Pre-load the rewarded HINT ad so the hint dialog can show instantly.
    LaunchedEffect(adsRemoved) {
        if (!adsRemoved) rewardedAdViewModel.loadRewardedAd(context, RewardType.HINT)
    }

    LaunchedEffect(difficultyString, categoryId, isDailyChallenge) {
        if (uiState.value.gameStatus != null) return@LaunchedEffect
        if (isDailyChallenge) {
            gameViewModel.initializeDailyChallenge()
        } else {
            gameViewModel.initializeGame(
                difficulty = Difficulty.valueOf(difficultyString),
                categoryId = categoryId,
                challengeMode = GameViewModel.consumePendingChallengeMode()
            )
        }
    }
    val gameStatus = uiState.value.gameStatus
    val guessResult = uiState.value.guessResult
    val error = uiState.value.error
    val isLoading = uiState.value.isLoading

    // Error state
    if (error != null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Error", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
            }
        }
        return
    }

    // Loading state
    if (gameStatus == null && isLoading) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎯 Loading word...", fontSize = 18.sp)
            }
        }
        return
    }

    if (gameStatus == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No game data available")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
            }
        }
        return
    }

    // Timed sessions end on a summary overlay below — the per-word LOST result screen
    // would fire as soon as the clock zeroes out an in-flight word, so skip it.
    val isTimedSession = gameStatus.mode.isTimed
    if (uiState.value.showingTimedSummary && gameStatus.mode.isTimed) {
        TimedSessionSummary(
            navController = navController,
            gameViewModel = gameViewModel,
            uiState = uiState.value
        )
        return
    }

    // Show loss results screen
    if (gameStatus.isGameOver && gameStatus.state == GameState.LOST && !isTimedSession) {
        if (isDailyChallenge) {
            DailyChallengeResultScreen(
                navController = navController,
                gameViewModel = gameViewModel
            )
        } else {
            GameResultsScreen(navController, gameStatus, gameViewModel)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when {
                                isDailyChallenge -> "📅 Daily Challenge"
                                gameStatus.mode.isTimed -> "⏱️ Timed Blitz"
                                else -> "🎯 Level ${gameStatus.levelIndex}"
                            },
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.value.winStreak > 0) {
                            Text(
                                "🔥 Win Streak: ${uiState.value.winStreak}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Text(
                        "🪙 $tokenBalance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            if (gameStatus.isPaused) gameViewModel.resumeGame()
                            else gameViewModel.pauseGame()
                        },
                        enabled = !gameStatus.isGameOver
                    ) {
                        Text(if (gameStatus.isPaused) "▶" else "⏸", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score / Attempts row (+ timer or word-count chip depending on mode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScoreChip(label = "Score", value = "${gameStatus.score}")
                    DifficultyChip(difficulty = gameStatus.word.difficulty.name)
                    if (gameStatus.mode.isTimed) {
                        TimerChip(
                            secondsLeft = gameStatus.timeRemainingSeconds ?: 0L,
                            wordsSolved = uiState.value.timedWordsSolved
                        )
                    } else {
                        AttemptsChip(remaining = gameStatus.remainingAttempts, max = gameStatus.word.difficulty.maxAttempts)
                    }
                }

                // Challenge mode indicator
                val challengeMode = uiState.value.challengeConfig.mode
                if (challengeMode != ChallengeMode.CLASSIC) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (challengeMode) {
                            ChallengeMode.TIMED -> Color(0xFF3498DB).copy(alpha = 0.15f)
                            ChallengeMode.LIMITED_GUESSES -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                            ChallengeMode.CATEGORY_CHALLENGE -> Color(0xFF9B59B6).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = challengeMode.icon,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = challengeMode.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (challengeMode) {
                                    ChallengeMode.TIMED -> Color(0xFF3498DB)
                                    ChallengeMode.LIMITED_GUESSES -> Color(0xFFE74C3C)
                                    ChallengeMode.CATEGORY_CHALLENGE -> Color(0xFF9B59B6)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (challengeMode == ChallengeMode.CATEGORY_CHALLENGE) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📂 ${uiState.value.categoryWordsCompleted} completed",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (challengeMode == ChallengeMode.LIMITED_GUESSES) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚠️ Max 3 attempts",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE74C3C)
                                )
                            }
                        }
                    }
                }

                // Classic level clue — shown up-front, so the player knows what the word is about.
                gameStatus.visibleClue?.let { clue ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🔎 CLUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                letterSpacing = 2.sp
                            )
                            Text(
                                clue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Hangman drawing
                HangmanDrawing(
                    wrongGuesses = gameStatus.incorrectGuesses.size,
                    maxAttempts = gameStatus.word.difficulty.maxAttempts,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Wrong guesses display
                if (gameStatus.incorrectGuesses.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("❌ ", fontSize = 12.sp)
                            Text(
                                gameStatus.incorrectGuesses.sorted().joinToString(" "),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Word display
                key(gameStatus.revealedWord) {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                        exit = scaleOut(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100))
                    ) {
                        Text(
                            text = gameStatus.revealedWord.replace("", " ").trim(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }

                // Guess feedback
                AnimatedVisibility(
                    visible = guessResult != null && guessResult != GuessResult.Invalid,
                    enter = scaleIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200))
                ) {
                    if (guessResult != null && guessResult != GuessResult.Invalid) {
                        val (feedbackText, feedbackColor) = when (guessResult) {
                            GuessResult.Correct -> "✓ Correct!" to Color(0xFF2ECC71)
                            GuessResult.Incorrect -> "✗ Wrong!" to Color(0xFFE74C3C)
                            GuessResult.AlreadyGuessed -> "Already guessed" to Color(0xFFF39C12)
                            else -> "" to Color.Transparent
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = feedbackColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                feedbackText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = feedbackColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hint message
                uiState.value.hintMessage?.let { message ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Combo indicator (visible when 2+ consecutive correct guesses)
                if (gameStatus.currentCombo >= 2) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFF6B35).copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${gameStatus.currentCombo}x Combo!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "+${gameStatus.currentCombo + 1}🪙 next guess",
                                fontSize = 11.sp,
                                color = Color(0xFFFF6B35).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Hints panel (shown only when Hints & Power-ups is enabled in Settings)
                // Power-up availability per mode:
                //   Timed Blitz     -> Clue, Reveal, Clear Wrong, Skip (no Extra Life)
                //   Classic         -> Reveal, Clear Wrong, Extra Life (no Clue — shown up-front)
                //   Daily Challenge -> Reveal, Clear Wrong only
                val availableHints = when {
                    gameStatus.mode.isTimed ->
                        HintType.entries - HintType.EXTRA_LIFE
                    isDailyChallenge ->
                        HintType.entries - HintType.CLUE - HintType.SKIP_WORD - HintType.EXTRA_LIFE
                    else ->
                        HintType.entries - HintType.CLUE
                }
                if (isHintsEnabled && availableHints.isNotEmpty()) {
                    HintsPanel(
                        hints = availableHints,
                        tokenBalance = tokenBalance,
                        enabled = !gameStatus.isPaused,
                        costOf = { gameViewModel.effectiveHintCost(it, ownedPerks) },
                        onUseHint = { hint ->
                            val cost = gameViewModel.effectiveHintCost(hint, ownedPerks)
                            if (tokenBalance < cost && !adsRemoved) {
                                pendingHintType = hint
                                showHintAdDialog = true
                            } else {
                                gameViewModel.useHint(hint)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alphabet grid
                AlphabetGrid(
                    guessedLetters = gameStatus.guessedLetters,
                    incorrectLetters = gameStatus.incorrectGuesses,
                    onLetterClick = { letter ->
                        gameViewModel.guessLetter(letter)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Pause overlay
            if (gameStatus.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⏸ Paused", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp))
                            Button(
                                onClick = { gameViewModel.resumeGame() },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) { Text("▶ Resume") }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) { Text("Quit Game") }
                        }
                    }
                }
            }

            // Win celebration overlay
            if (uiState.value.showingWinCelebration) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.88f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Correct!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2ECC71)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The word was: ${gameStatus.word.normalizedValue}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            gameStatus.approvedStarRating?.let { stars ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = buildString {
                                        repeat(stars) { append("★") }
                                        repeat(3 - stars) { append("☆") }
                                    },
                                    fontSize = 30.sp,
                                    color = Color(0xFFF1C40F),
                                    letterSpacing = 4.sp
                                )
                                Text(
                                    "Solved in ${gameStatus.elapsedSeconds}s",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🔥 Streak: ${uiState.value.winStreak}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE67E22)
                            )
                            Text(
                                "🪙 +${uiState.value.tokensEarnedThisGame} tokens",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { gameViewModel.continueAfterWin() },
                                enabled = !uiState.value.isProcessingAction,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2ECC71)
                                )
                            ) {
                                Text(
                                    "▶ Next Word",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    gameViewModel.resetGame()
                                    navController.navigate(NavigationRoute.Home.route) {
                                        popUpTo(NavigationRoute.Home.route) { inclusive = true }
                                    }
                                },
                                enabled = !uiState.value.isProcessingAction,
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) { Text("🏠 Go Home") }
                        }
                    }
                }
            }

            // Daily challenge win overlay — player taps Go Home to leave, giving
            // recordCompletion() time to persist before the ViewModel is cleared.
            if (uiState.value.showingDailyWinCelebration) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.88f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Daily Challenge Complete!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2ECC71),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The word was: ${gameStatus.word.normalizedValue}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🪙 +${uiState.value.tokensEarnedThisGame} tokens earned",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    gameViewModel.dismissDailyWinCelebration()
                                    gameViewModel.resetGame()
                                    navController.navigate(NavigationRoute.Home.route) {
                                        popUpTo(NavigationRoute.Home.route) { inclusive = true }
                                    }
                                },
                                enabled = !uiState.value.isProcessingAction,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2ECC71)
                                )
                            ) {
                                Text(
                                    "🏠 Go Home",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Achievement notification overlay
    Box(modifier = Modifier.fillMaxSize()) {
        AchievementNotification(
            achievement = notificationState.achievement,
            isVisible = notificationState.isVisible,
            onDismiss = { notificationViewModel.dismissNotification() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    LaunchedEffect(uiState.value.gameStatus?.isGameOver) {
        if (uiState.value.gameStatus?.isGameOver == true) {
            notificationViewModel.checkAndShowNotification()
        }
    }

    // ── Hint insufficient-tokens dialog: offer a rewarded ad for +20 tokens ─
    if (showHintAdDialog && !adsRemoved) {
        AlertDialog(
            onDismissRequest = { showHintAdDialog = false },
            title = { Text("Not Enough Tokens") },
            text = { Text("Watch a short ad to get +20 🪙 tokens, then use your hint!") },
            confirmButton = {
                Button(
                    onClick = {
                        showHintAdDialog = false
                        val activity = context.findActivity()
                        if (activity != null) {
                            val hint = pendingHintType
                            pendingHintType = null
                            rewardedAdViewModel.showRewardedAd(
                                RewardType.HINT, activity, context
                            ) {
                                rewardedAdViewModel.grantTokens(20)
                                if (hint != null) {
                                    gameViewModel.useHint(hint)
                                }
                            }
                        } else {
                            rewardedAdViewModel.loadRewardedAd(context, RewardType.HINT)
                        }
                    }
                ) { Text("📺 Watch Ad (+20 🪙)") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showHintAdDialog = false }) {
                    Text("Back to Game")
                }
            }
        )
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

@Composable
private fun DailyChallengeResultScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    adViewModel: AdViewModel = hiltViewModel(),
    rewardedAdViewModel: RewardedAdViewModel = hiltViewModel()
) {
    val adState = adViewModel.adState.collectAsState().value
    val context = LocalContext.current
    val adRetryAvailable by gameViewModel.dailyAdRetryAvailable.collectAsState()
    val adsRemoved by rewardedAdViewModel.adsRemoved.collectAsState()

    LaunchedEffect(adRetryAvailable, adsRemoved) {
        if (adRetryAvailable && !adsRemoved) {
            rewardedAdViewModel.loadRewardedAd(context, RewardType.DAILY_RETRY)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💀", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Daily Challenge Over",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (adRetryAvailable) {
                Text(
                    "Watch an ad to try again!",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    "Better luck tomorrow!",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (adRetryAvailable) {
                Button(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            val shown = rewardedAdViewModel.showRewardedAd(
                                RewardType.DAILY_RETRY, activity, context
                            ) {
                                gameViewModel.useDailyAdRetry()
                                gameViewModel.initializeDailyChallenge()
                            }
                            if (!shown) {
                                rewardedAdViewModel.loadRewardedAd(context, RewardType.DAILY_RETRY)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("📺 Watch Ad & Retry", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            OutlinedButton(
                onClick = {
                    gameViewModel.resetGame()
                    navController.navigate(NavigationRoute.Home.route) {
                        popUpTo(NavigationRoute.Home.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("🏠 Back to Home")
            }
        }
    }
}

@Composable
private fun ScoreChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val color = when (difficulty) {
        "EASY" -> Color(0xFF27AE60)
        "MEDIUM" -> Color(0xFFF39C12)
        else -> Color(0xFFE74C3C)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            difficulty,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AttemptsChip(remaining: Int, max: Int) {
    val fraction = remaining.toFloat() / max
    val color = when {
        fraction > 0.6f -> Color(0xFF27AE60)
        fraction > 0.3f -> Color(0xFFF39C12)
        else -> Color(0xFFE74C3C)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lives", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$remaining", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

/**
 * Draws the hangman figure progressively as [wrongGuesses] increases.
 * Parts drawn in order: gallows w/ braces (always), head (with a face), body,
 * left arm, right arm, left leg, right leg. Each newly added part grows in with
 * a short spring-in animation driven by [animateFloatAsState] on the part counter.
 */
@Composable
private fun HangmanDrawing(
    wrongGuesses: Int,
    maxAttempts: Int,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onBackground
    val wrongColor = Color(0xFFE74C3C)

    // Animated completion fraction for each body part (0.0 – 1.0). Parts appear in
    // order, so part N animates as the counter crosses N.
    val partProgresses = (1..6).map { part ->
        animateFloatAsState(
            targetValue = if (wrongGuesses >= part) 1f else 0f,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            label = "hangman_part_$part"
        ).value
    }
    val headP = partProgresses[0]
    val bodyP = partProgresses[1]
    val leftArmP = partProgresses[2]
    val rightArmP = partProgresses[3]
    val leftLegP = partProgresses[4]
    val rightLegP = partProgresses[5]

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 6f

        // ── Gallows — always visible, with diagonal braces + a drawArc ground swoosh
        drawArc(
            color = lineColor.copy(alpha = 0.25f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.12f, h * 0.86f),
            size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.12f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeW * 0.6f, cap = StrokeCap.Round)
        )
        drawLine(lineColor, Offset(w * 0.15f, h * 0.95f), Offset(w * 0.85f, h * 0.95f), strokeW, StrokeCap.Round) // base
        drawLine(lineColor, Offset(w * 0.3f, h * 0.95f), Offset(w * 0.3f, h * 0.05f), strokeW, StrokeCap.Round)  // pole
        drawLine(lineColor, Offset(w * 0.3f, h * 0.05f), Offset(w * 0.6f, h * 0.05f), strokeW, StrokeCap.Round)  // top bar
        drawLine(lineColor, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.45f, h * 0.05f), strokeW * 0.7f, StrokeCap.Round) // brace
        drawLine(lineColor, Offset(w * 0.6f, h * 0.05f), Offset(w * 0.6f, h * 0.18f), strokeW, StrokeCap.Round)  // rope

        val headCenter = Offset(w * 0.6f, h * 0.29f)
        val headRadius = h * 0.1f

        // ── Head (with a worried face once fully drawn)
        if (headP > 0f) {
            drawCircle(
                wrongColor,
                radius = headRadius * headP,
                center = headCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeW)
            )
            if (headP > 0.85f) {
                // Eyes (little crosses-of-despair) + mouth arc once the head settles
                val eyeY = headCenter.y - headRadius * 0.2f
                val eyeDX = headRadius * 0.38f
                drawCircle(wrongColor, radius = strokeW * 0.5f, center = Offset(headCenter.x - eyeDX, eyeY))
                drawCircle(wrongColor, radius = strokeW * 0.5f, center = Offset(headCenter.x + eyeDX, eyeY))
                drawArc(
                    color = wrongColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(headCenter.x - headRadius * 0.4f, headCenter.y + headRadius * 0.15f),
                    size = androidx.compose.ui.geometry.Size(headRadius * 0.8f, headRadius * 0.5f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeW * 0.6f, cap = StrokeCap.Round)
                )
            }
        }

        val neckY = h * 0.39f
        val hipY = h * 0.67f
        val shoulderY = h * 0.46f
        val feetY = h * 0.85f

        // ── Body grows downward from the neck
        if (bodyP > 0f) {
            drawLine(
                wrongColor,
                Offset(w * 0.6f, neckY),
                Offset(w * 0.6f, neckY + (hipY - neckY) * bodyP),
                strokeW, StrokeCap.Round
            )
        }

        // ── Arms swing outward from the shoulders
        if (leftArmP > 0f) {
            val shoulder = Offset(w * 0.6f, shoulderY)
            val hand = Offset(w * 0.45f, h * 0.57f)
            drawLine(
                wrongColor,
                shoulder,
                shoulder + (hand - shoulder) * leftArmP,
                strokeW, StrokeCap.Round
            )
        }
        if (rightArmP > 0f) {
            val shoulder = Offset(w * 0.6f, shoulderY)
            val hand = Offset(w * 0.75f, h * 0.57f)
            drawLine(
                wrongColor,
                shoulder,
                shoulder + (hand - shoulder) * rightArmP,
                strokeW, StrokeCap.Round
            )
        }

        // ── Legs kick down from the hips
        if (leftLegP > 0f) {
            val hip = Offset(w * 0.6f, hipY)
            val foot = Offset(w * 0.45f, feetY)
            drawLine(
                wrongColor,
                hip,
                hip + (foot - hip) * leftLegP,
                strokeW, StrokeCap.Round
            )
        }
        if (rightLegP > 0f) {
            val hip = Offset(w * 0.6f, hipY)
            val foot = Offset(w * 0.75f, feetY)
            drawLine(
                wrongColor,
                hip,
                hip + (foot - hip) * rightLegP,
                strokeW, StrokeCap.Round
            )
        }
    }
}

/** Big countdown + solved-word counter used while [GameMode.TIMED] is active. */
@Composable
private fun TimerChip(secondsLeft: Long, wordsSolved: Int) {
    val color = when {
        secondsLeft > 30L -> Color(0xFF27AE60)
        secondsLeft > 10L -> Color(0xFFF39C12)
        else -> Color(0xFFE74C3C)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⏱ ${secondsLeft}s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(
                "$wordsSolved word${if (wordsSolved == 1) "" else "s"}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Final overlay for a timed session: shows how many words were solved and the
 * coin payout, then offers Play Again or Home.
 */
@Composable
private fun TimedSessionSummary(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    uiState: com.LetterQuest.ui.viewmodel.GameUIState
) {
    val wordsSolved = uiState.timedWordsSolved
    val payout = uiState.tokensEarnedThisGame

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⏱️", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Time's Up!", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You solved $wordsSolved word${if (wordsSolved == 1) "" else "s"} in ${GameMode.TIMED_SESSION_SECONDS}s!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1C40F).copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Session Reward",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "🪙 +$payout token${if (payout == 1) "" else "s"}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF39C12)
                    )
                    if (uiState.tokensEarnedThisGame > payout) {
                        Text(
                            "(incl. 🪙 +${uiState.tokensEarnedThisGame - payout} from guesses)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { gameViewModel.restartTimedSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
            ) {
                Text("↻ Play Again", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    gameViewModel.resetGame()
                    navController.navigate(NavigationRoute.Home.route) {
                        popUpTo(NavigationRoute.Home.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("🏠 Back to Home")
            }
        }
    }
}

@Composable
private fun HintsPanel(
    hints: List<HintType>,
    tokenBalance: Int,
    enabled: Boolean,
    costOf: (HintType) -> Int,
    onUseHint: (HintType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "⚡ Power-ups",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                hints.forEach { hint ->
                    val cost = costOf(hint)
                    val affordable = tokenBalance >= cost
                    OutlinedButton(
                        onClick = { onUseHint(hint) },
                        enabled = enabled && affordable,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (affordable) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(hint.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                            Text("🪙 $cost", fontSize = 10.sp, color = if (affordable) Color(0xFFF39C12) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlphabetGrid(
    guessedLetters: Set<Char>,
    incorrectLetters: Set<Char>,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').toList()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        alphabet.chunked(6).forEach { rowLetters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowLetters.forEach { letter ->
                    val isCorrect = letter in guessedLetters
                    val isWrong = letter in incorrectLetters
                    val isUsed = isCorrect || isWrong
                    AnimatedVisibility(
                        visible = !isUsed,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                        exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLetterClick(letter) },
                            enabled = !isUsed,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(letter.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Show placeholder tile for used letters
                    if (isUsed) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCorrect) Color(0xFF27AE60).copy(alpha = 0.2f)
                            else Color(0xFFE74C3C).copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    letter.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF27AE60) else Color(0xFFE74C3C)
                                )
                            }
                        }
                    }
                }
                repeat(6 - rowLetters.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
