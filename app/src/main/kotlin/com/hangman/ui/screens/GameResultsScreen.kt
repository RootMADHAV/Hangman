package com.hangman.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hangman.domain.model.GameStatus
import com.hangman.domain.model.GameState
import com.hangman.domain.model.RewardType
import com.hangman.ui.components.AchievementNotification
import com.hangman.ui.components.BannerAd
import com.hangman.ui.components.InterstitialAd
import com.hangman.ui.components.RewardedAdCard
import com.hangman.ui.navigation.NavigationRoute
import com.hangman.ui.viewmodel.AdViewModel
import com.hangman.ui.viewmodel.GameViewModel
import com.hangman.ui.viewmodel.NotificationViewModel
import com.hangman.ui.viewmodel.RewardedAdViewModel
import com.hangman.ui.viewmodel.SoundViewModel
import com.hangman.util.findActivity

@Composable
fun GameResultsScreen(
    navController: NavHostController,
    gameStatus: GameStatus,
    gameViewModel: GameViewModel,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    adViewModel: AdViewModel = hiltViewModel(),
    soundViewModel: SoundViewModel = hiltViewModel(),
    rewardedAdViewModel: RewardedAdViewModel = hiltViewModel()
) {
    val notificationState = notificationViewModel.notificationState.collectAsState().value
    val adState = adViewModel.adState.collectAsState().value
    val uiState = gameViewModel.uiState.collectAsState().value
    val winStreak = uiState.winStreak
    val totalTokens = uiState.totalTokensEarned + uiState.tokensEarnedThisGame
    val isWin = gameStatus.state == GameState.WON
    val adsRemoved by rewardedAdViewModel.adsRemoved.collectAsState()
    var showContinueDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Pre-load rewarded ads so the offers below can play immediately.
    LaunchedEffect(adsRemoved) {
        if (adsRemoved) return@LaunchedEffect
        rewardedAdViewModel.loadRewardedAd(context, RewardType.COINS)
        rewardedAdViewModel.loadRewardedAd(context, RewardType.HINT)
    }

    // Count this finished game; show the interstitial only after every 10 games.
    LaunchedEffect(Unit) {
        rewardedAdViewModel.recordGameCompleted()
        if (rewardedAdViewModel.shouldShowInterstitial()) {
            // Load through AdManager, then surface the existing overlay only when
            // a real ad is ready. Counter resets when the overlay is dismissed.
            rewardedAdViewModel.loadInterstitialAd(context) {
                adViewModel.showInterstitialAd()
            }
        }
    }

    LaunchedEffect(Unit) {
        soundViewModel.playLoseSound()
    }

    // Every loss shows the continue-ad dialog exactly once per results visit.
    LaunchedEffect(isWin, adsRemoved) {
        if (!isWin && !adsRemoved) showContinueDialog = true
    }

    Scaffold(
        bottomBar = { BannerAd(modifier = Modifier.fillMaxWidth()) }
    ) { padding ->
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Title ────────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💀",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Game Over",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE74C3C),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // ── The word ─────────────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "The word was:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            gameStatus.word.displayValue,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (!gameStatus.word.hint.isNullOrBlank()) {
                            Text(
                                "💡 ${gameStatus.word.hint}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // ── Win streak (if they had one) ──────────────────────────────
                if (winStreak > 0) {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF39C12).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("🔥 Best Streak", fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$winStreak words!", fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("🪙 Tokens Earned", fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+$totalTokens", fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // ── Score & stats ─────────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Score", "${gameStatus.score}")
                        StatItem("Correct", "${gameStatus.correctGuesses.size}")
                        StatItem("Wrong", "${gameStatus.incorrectGuesses.size}")
                    }
                }

                // Tokens earned this final round
                if (uiState.tokensEarnedThisGame > 0) {
                    Text(
                        "🪙 +${uiState.tokensEarnedThisGame} tokens earned",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Rewarded ad offers (hidden once ads are removed) ──────────
                if (isWin && uiState.tokensEarnedThisGame > 0 && !adsRemoved) {
                    // "Double rewards" — grants an extra tokensEarnedThisGame via COINS
                    // once the SDK reports an earned reward. The hardcoded amount from
                    // the reward API is ignored: we match what the player actually won.
                    RewardedAdCard(
                        rewardText = "2x Rewards!",
                        description = "Watch an ad to double your tokens this game",
                        onWatchClick = {
                            val activity = context.findActivity()
                            if (activity != null) {
                                rewardedAdViewModel.showRewardedAd(
                                    RewardType.COINS, activity, context
                                ) {
                                    rewardedAdViewModel.grantTokens(uiState.tokensEarnedThisGame)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Actions ──────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
                ) {
                    Button(
                        onClick = {
                            gameViewModel.resetGame()
                            navController.navigate(NavigationRoute.GameSetup.route) {
                                popUpTo(NavigationRoute.GameSetup.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("🎮 New Game", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
                ) {
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

            AchievementNotification(
                achievement = notificationState.achievement,
                isVisible = notificationState.isVisible,
                onDismiss = { notificationViewModel.dismissNotification() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            InterstitialAd(
                isVisible = adState.showInterstitialAd,
                onDismiss = {
                    adViewModel.dismissInterstitialAd()
                    rewardedAdViewModel.resetInterstitialCounter()
                }
            )
        }
    }
    }

    // ── Continue dialog (per-loss, one shot): watch ad for +20 tokens ────────
    if (!isWin && !adsRemoved && showContinueDialog) {
        AlertDialog(
            onDismissRequest = { showContinueDialog = false },
            title = { Text("📺 Continue Playing?") },
            text = { Text("Watch a short ad to get +20 🪙 tokens and keep your streak alive!") },
            confirmButton = {
                Button(
                    onClick = {
                        showContinueDialog = false
                        val activity = context.findActivity()
                        if (activity != null) {
                            // Grants the spec-mandated +20 regardless of SDK reward amount.
                            rewardedAdViewModel.showRewardedAd(
                                RewardType.HINT, activity, context
                            ) {
                                rewardedAdViewModel.grantTokens(20)
                            }
                        }
                    }
                ) { Text("📺 Watch Ad (+20 🪙)") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showContinueDialog = false
                        gameViewModel.resetGame()
                        navController.navigate(NavigationRoute.Home.route) {
                            popUpTo(NavigationRoute.Home.route) { inclusive = true }
                        }
                    }
                ) { Text("🏠 Back to Home") }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
