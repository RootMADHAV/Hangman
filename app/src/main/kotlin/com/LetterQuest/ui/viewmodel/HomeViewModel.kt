package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.PlayerLevel
import com.LetterQuest.domain.repository.StatisticsRepository
import com.LetterQuest.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    val statistics: StateFlow<com.LetterQuest.domain.model.PlayerStatistics> = statisticsRepository
        .observeStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = com.LetterQuest.domain.model.PlayerStatistics()
        )

    val achievements: StateFlow<List<com.LetterQuest.domain.model.Achievement>> = achievementRepository
        .observeAchievements()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val playerLevel: StateFlow<PlayerLevel> = statisticsRepository
        .observeStatistics()
        .map { stats -> PlayerLevel.from(stats.gamesWon, stats.totalScore) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PlayerLevel.from(0, 0)
        )

    fun resetStatistics() {
        viewModelScope.launch {
            statisticsRepository.resetStatistics()
        }
    }
}
