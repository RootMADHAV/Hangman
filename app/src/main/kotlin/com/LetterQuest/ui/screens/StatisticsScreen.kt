@file:OptIn(ExperimentalMaterial3Api::class)

package com.LetterQuest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.LetterQuest.ui.components.BannerAd
import com.LetterQuest.ui.viewmodel.AdViewModel
import com.LetterQuest.ui.viewmodel.HomeViewModel

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    adViewModel: AdViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val statistics = homeViewModel.statistics.collectAsState()
    val stats = statistics.value
    val adState = adViewModel.adState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.semantics { contentDescription = "Navigate back" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = { BannerAd(modifier = Modifier.fillMaxWidth(), showAds = adState.showBannerAd) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Your Statistics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            StatisticCard(
                title = "Games Played",
                value = "${stats.gamesPlayed}"
            )

            StatisticCard(
                title = "Wins",
                value = "${stats.gamesWon}"
            )

            StatisticCard(
                title = "Losses",
                value = "${stats.gamesLost}"
            )

            StatisticCard(
                title = "Win Rate",
                value = "${String.format("%.1f", stats.winPercentage)}%"
            )

            StatisticCard(
                title = "Highest Score",
                value = "${stats.highestScore}"
            )

            StatisticCard(
                title = "Average Score",
                value = "${String.format("%.1f", stats.averageScoreCalculated)}"
            )

            StatisticCard(
                title = "Total Score",
                value = "${stats.totalScore}"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { homeViewModel.resetStatistics() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Reset Statistics")
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
