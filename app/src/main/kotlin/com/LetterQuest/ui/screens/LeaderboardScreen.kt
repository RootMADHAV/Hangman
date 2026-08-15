package com.LetterQuest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.data.repository.WordCatalog
import com.LetterQuest.domain.model.ChallengeMode
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.LeaderboardFilterConfig
import com.LetterQuest.domain.model.LeaderboardSortBy
import com.LetterQuest.domain.model.LeaderboardTimeFilter
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.ui.components.BannerAd
import com.LetterQuest.ui.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboard = viewModel.leaderboard.collectAsState().value
    val filterConfig by viewModel.filterConfig.collectAsState()

    var expandedTime by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedDifficulty by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = { BannerAd(modifier = Modifier.fillMaxWidth()) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sort filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedSort,
                    onExpandedChange = { expandedSort = !expandedSort },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedButton(
                        onClick = { expandedSort = !expandedSort },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    ) {
                        Text("${filterConfig.sortBy.displayName} ▾")
                    }
                    ExposedDropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false }
                    ) {
                        LeaderboardSortBy.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName) },
                                onClick = {
                                    viewModel.setFilter(filterConfig.copy(sortBy = sort))
                                    expandedSort = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedTime,
                    onExpandedChange = { expandedTime = !expandedTime },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedButton(
                        onClick = { expandedTime = !expandedTime },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    ) {
                        Text("${filterConfig.timeFilter.displayName} ▾")
                    }
                    ExposedDropdownMenu(
                        expanded = expandedTime,
                        onDismissRequest = { expandedTime = false }
                    ) {
                        LeaderboardTimeFilter.entries.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time.displayName) },
                                onClick = {
                                    viewModel.setFilter(filterConfig.copy(timeFilter = time))
                                    expandedTime = false
                                }
                            )
                        }
                    }
                }
            }

            // Category and Difficulty filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedButton(
                        onClick = { expandedCategory = !expandedCategory },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    ) {
                        Text(
                            text = filterConfig.selectedCategory?.let { catId ->
                                WordCatalog.categories.find { it.id == catId }?.name ?: "All Categories"
                            } ?: "All Categories",
                            maxLines = 1
                        )
                        Text(" ▾", modifier = Modifier.padding(start = 4.dp))
                    }
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                viewModel.setFilter(filterConfig.copy(selectedCategory = WordCategory.ALL_CATEGORIES_ID))
                                expandedCategory = false
                            }
                        )
                        WordCatalog.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.icon} ${category.name}") },
                                onClick = {
                                    viewModel.setFilter(filterConfig.copy(selectedCategory = category.id))
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedDifficulty,
                    onExpandedChange = { expandedDifficulty = !expandedDifficulty },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedButton(
                        onClick = { expandedDifficulty = !expandedDifficulty },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    ) {
                        Text(
                            text = filterConfig.difficultyFilter?.name ?: "All Difficulties",
                            maxLines = 1
                        )
                        Text(" ▾", modifier = Modifier.padding(start = 4.dp))
                    }
                    ExposedDropdownMenu(
                        expanded = expandedDifficulty,
                        onDismissRequest = { expandedDifficulty = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Difficulties") },
                            onClick = {
                                viewModel.setFilter(filterConfig.copy(difficultyFilter = null))
                                expandedDifficulty = false
                            }
                        )
                        Difficulty.entries.forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff.name) },
                                onClick = {
                                    viewModel.setFilter(filterConfig.copy(difficultyFilter = diff))
                                    expandedDifficulty = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (leaderboard.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No games match this filter", fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(leaderboard) { index, entry ->
                        LeaderboardCard(
                            rank = index + 1,
                            word = entry.word,
                            difficulty = entry.difficulty.name,
                            score = entry.score,
                            elapsedSeconds = entry.elapsedSeconds,
                            category = entry.category ?: ""
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    word: String,
    difficulty: String,
    score: Int,
    elapsedSeconds: Long,
    category: String = ""
) {
    val medalEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "•"
    }

    val backgroundColor = when (rank) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.surfaceVariant
        3 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$medalEmoji #$rank",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(60.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$difficulty${if (category.isNotBlank()) " • $category" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$score pts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${elapsedSeconds}s",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
