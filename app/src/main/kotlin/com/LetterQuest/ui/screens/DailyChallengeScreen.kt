@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.DailyChallenge
import com.LetterQuest.domain.model.RewardType
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.DailyChallengeViewModel
import com.LetterQuest.ui.viewmodel.GameViewModel
import com.LetterQuest.ui.viewmodel.RewardedAdViewModel
import com.LetterQuest.util.findActivity

@Composable
fun DailyChallengeScreen(
    navController: NavHostController,
    viewModel: DailyChallengeViewModel = hiltViewModel(),
    gameViewModel: GameViewModel = hiltViewModel(),
    rewardedAdViewModel: RewardedAdViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val streak = viewModel.streak.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(uiState.adRetryAvailable) {
        if (uiState.adRetryAvailable) {
            rewardedAdViewModel.loadRewardedAd(context, RewardType.DAILY_RETRY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📅", fontSize = 56.sp, modifier = Modifier.padding(bottom = 8.dp))

                Text(
                    "Today's Puzzle",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StreakStat("Current Streak", "${streak.current}", Modifier.weight(1f))
                    StreakStat("Best Streak", "${streak.longest}", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.hintText != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Clue",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                uiState.hintText,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                when {
                    uiState.isLoading -> {
                        Text("Loading today's puzzle...", fontSize = 15.sp)
                    }

                    uiState.isCompleted && uiState.wasWon -> {
                        Text(
                            "✅ Completed today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "A new puzzle arrives tomorrow.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    uiState.hasAttempted && !uiState.adRetryAvailable -> {
                        Text(
                            "Better luck tomorrow!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "A new puzzle arrives tomorrow.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    else -> {
                        Text(
                            if (uiState.hasAttempted && uiState.adRetryAvailable) {
                                "Watch an ad for a second chance!"
                            } else {
                                "Win to earn a 🪙 ${DailyChallenge.COMPLETION_BONUS_TOKENS} bonus " +
                                    "and extend your streak."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                if (uiState.hasAttempted && uiState.adRetryAvailable) {
                                    val activity = context.findActivity()
                                    if (activity != null) {
                                        val shown = rewardedAdViewModel.showRewardedAd(
                                            RewardType.DAILY_RETRY, activity, context
                                        ) {
                                            gameViewModel.useDailyAdRetry()
                                            navController.navigate(NavigationRoute.DailyGameplay.route)
                                        }
                                        if (!shown) {
                                            rewardedAdViewModel.loadRewardedAd(context, RewardType.DAILY_RETRY)
                                        }
                                    }
                                } else {
                                    navController.navigate(NavigationRoute.DailyGameplay.route)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                if (uiState.hasAttempted && uiState.adRetryAvailable) {
                                    "📺 Watch Ad & Retry"
                                } else {
                                    "Play Today's Puzzle"
                                },
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
