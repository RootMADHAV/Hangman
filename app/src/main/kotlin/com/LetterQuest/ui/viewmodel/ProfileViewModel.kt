package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.PlayerStatistics
import com.LetterQuest.domain.repository.AchievementRepository
import com.LetterQuest.domain.repository.GameHistoryRepository
import com.LetterQuest.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUIState(
    val statistics: PlayerStatistics = PlayerStatistics(),
    val achievements: List<Achievement> = emptyList(),
    val totalGames: Int = 0,
    val winPercentage: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val achievementRepository: AchievementRepository,
    private val gameHistoryRepository: GameHistoryRepository
) : ViewModel() {

    val profileState: StateFlow<ProfileUIState> = combine(
        statisticsRepository.observeStatistics(),
        achievementRepository.observeAchievements(),
        gameHistoryRepository.observeAllGames()
    ) { statistics, achievements, gameHistory ->
        val totalGames = gameHistory.size
        val wonGames = gameHistory.count { it.won }
        val winPercentage = if (totalGames > 0) (wonGames * 100.0) / totalGames else 0.0

        ProfileUIState(
            statistics = statistics,
            achievements = achievements,
            totalGames = totalGames,
            winPercentage = winPercentage,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ProfileUIState()
    )
}
