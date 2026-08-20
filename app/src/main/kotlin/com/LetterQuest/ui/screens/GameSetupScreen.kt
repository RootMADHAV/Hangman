@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.domain.model.ChallengeMode
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameMode
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.ui.navigation.NavigationRoute
import com.LetterQuest.ui.viewmodel.GameViewModel

/**
 * Setup flow: pick a game mode first (Classic levels vs 60s Timed Blitz), then a
 * difficulty; the category step comes next via [NavigationRoute.CategorySelect].
 */
@Composable
fun GameSetupScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel = hiltViewModel()
) {
    var selectedMode by rememberSaveable { mutableStateOf(ChallengeMode.CLASSIC.name) }
    var selectedDifficulty by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Setup") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                ChallengeMode.entries.forEach { mode ->
                    ModeCard(
                        mode = mode,
                        selected = selectedMode == mode.name,
                        onClick = { selectedMode = mode.name },
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )
                    if (mode != ChallengeMode.entries.last()) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Choose Your Difficulty",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DifficultyButton(
                text = "Easy",
                description = "10 attempts • 8-15 letter words",
                difficulty = Difficulty.EASY,
                selected = selectedDifficulty == Difficulty.EASY.name,
                onClick = { selectedDifficulty = Difficulty.EASY.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Medium",
                description = "8 attempts • 6-9 letter words",
                difficulty = Difficulty.MEDIUM,
                selected = selectedDifficulty == Difficulty.MEDIUM.name,
                onClick = { selectedDifficulty = Difficulty.MEDIUM.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Hard",
                description = "6 attempts • 4-6 letter words",
                difficulty = Difficulty.HARD,
                selected = selectedDifficulty == Difficulty.HARD.name,
                onClick = { selectedDifficulty = Difficulty.HARD.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val difficulty = selectedDifficulty ?: return@Button
                    val mode = ChallengeMode.valueOf(selectedMode)
                    GameViewModel.selectChallengeModeForNextGame(mode)
                    navController.navigate(
                        NavigationRoute.CategorySelect.routeWithDifficulty(difficulty)
                    )
                },
                enabled = selectedDifficulty != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (selectedDifficulty != null) {
                        "▶ Next: Pick a Category"
                    } else {
                        "Pick a difficulty to continue"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedMode == ChallengeMode.TIMED.name) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⏱️ ${GameMode.TIMED_SESSION_SECONDS}s on the clock • " +
                        "+${UserTokens.EARNED_PER_TIMED_WORD}🪙 per word solved • hints half price",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: ChallengeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(mode.icon, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(mode.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                mode.description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun DifficultyButton(
    text: String,
    description: String,
    difficulty: Difficulty,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
