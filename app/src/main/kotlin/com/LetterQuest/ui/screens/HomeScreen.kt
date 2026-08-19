@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.material3.LinearProgressIndicator
import com.LetterQuest.domain.model.PlayerLevel
import com.LetterQuest.ui.components.BannerAd
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.AdViewModel
import com.LetterQuest.ui.viewmodel.DailyChallengeViewModel
import com.LetterQuest.ui.viewmodel.GameViewModel
import com.LetterQuest.ui.viewmodel.HomeViewModel
import com.LetterQuest.ui.viewmodel.ShopViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    adViewModel: AdViewModel = hiltViewModel(),
    gameViewModel: GameViewModel = hiltViewModel(),
    dailyChallengeViewModel: DailyChallengeViewModel = hiltViewModel(),
    shopViewModel: ShopViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val adState = adViewModel.adState.collectAsState().value
    val tokenBalance = gameViewModel.tokenBalance.collectAsState().value
    val dailyStreak = dailyChallengeViewModel.streak.collectAsState().value
    val dailyLogin = shopViewModel.dailyLoginState.collectAsState().value
    val playerLevel = homeViewModel.playerLevel.collectAsState().value

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = if (adState.showBannerAd) 60.dp else 0.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // ── Hero header ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -60 }, animationSpec = tween(500)) +
                            fadeIn(animationSpec = tween(500))
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎮", fontSize = 52.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Letter Quest",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Guess the word before it's too late!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Token balance + daily login alert ────────────────────────
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🪙", fontSize = 22.sp)
                                Spacer(modifier = Modifier.padding(start = 6.dp))
                                Column {
                                    Text("Coins", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "$tokenBalance",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (dailyLogin.canClaim) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎁", fontSize = 22.sp)
                                    Text(
                                        "Daily reward!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🔥", fontSize = 22.sp)
                                    Text(
                                        "Streak: ${dailyStreak.current}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Level progress bar ────────────────────────────────────────
                LevelProgressCard(level = playerLevel)

                Spacer(modifier = Modifier.height(14.dp))

                // ── Primary play buttons ──────────────────────────────────────
                AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(700))) {
                    Button(
                        onClick = { navController.navigate(NavigationRoute.DailyChallenge.route) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            if (dailyStreak.current > 0)
                                "📅 Daily Challenge  🔥 ${dailyStreak.current}"
                            else "📅 Daily Challenge",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(800))) {
                    Button(
                        onClick = { navController.navigate(NavigationRoute.GameSetup.route) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("▶  Play Game", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Secondary buttons grid ────────────────────────────────────
                AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(900))) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                label = "🛒 Shop",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Shop.route) }
                            )
                            SecondaryButton(
                                label = "📊 Stats",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Statistics.route) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                label = "🏆 Achievements",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Achievements.route) }
                            )
                            SecondaryButton(
                                label = "📈 Progress",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.CategoryProgress.route) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                label = "🏅 Leaderboard",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Leaderboard.route) }
                            )
                            SecondaryButton(
                                label = "🎨 Themes",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.ThemeCustomization.route) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                label = "👤 Profile",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Profile.route) }
                            )
                            SecondaryButton(
                                label = "⚙️ Settings",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(NavigationRoute.Settings.route) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (adState.showBannerAd) {
                BannerAd(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LevelProgressCard(level: PlayerLevel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Lv ${level.level}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        level.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${level.currentXp} / ${level.xpForNextLevel} XP",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { level.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}
