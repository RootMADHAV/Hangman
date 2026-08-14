package com.hangman.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hangman.domain.model.Difficulty
import com.hangman.domain.model.GameHistoryEntry
import com.hangman.domain.model.LeaderboardFilterConfig
import com.hangman.domain.model.LeaderboardSortBy
import com.hangman.domain.model.LeaderboardTimeFilter
import com.hangman.domain.model.WordCategory
import com.hangman.domain.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val gameHistoryRepository: GameHistoryRepository
) : ViewModel() {

    private val _filterConfig = MutableStateFlow(LeaderboardFilterConfig())
    val filterConfig: StateFlow<LeaderboardFilterConfig> = _filterConfig.asStateFlow()

    val leaderboard = combine(
        gameHistoryRepository.observeAllGames(),
        _filterConfig
    ) { games, filter ->
        var filtered = games.filter { it.won }

        filtered = when (filter.timeFilter) {
            LeaderboardTimeFilter.TODAY -> filtered.filter { it.playedAt >= startOfToday() }
            LeaderboardTimeFilter.THIS_WEEK -> filtered.filter { it.playedAt >= startOfWeek() }
            LeaderboardTimeFilter.THIS_MONTH -> filtered.filter { it.playedAt >= startOfMonth() }
            LeaderboardTimeFilter.ALL_TIME -> filtered
        }

        if (filter.selectedCategory != null && filter.selectedCategory != WordCategory.ALL_CATEGORIES_ID) {
            filtered = filtered.filter { it.category == filter.selectedCategory }
        }

        filter.difficultyFilter?.let { diff ->
            filtered = filtered.filter { it.difficulty == diff }
        }

        filtered = when (filter.sortBy) {
            LeaderboardSortBy.SCORE_DESC -> filtered.sortedWith(compareBy<GameHistoryEntry> { -it.score }.thenBy { it.elapsedSeconds })
            LeaderboardSortBy.TIME_ASC -> filtered.sortedWith(compareBy<GameHistoryEntry> { it.elapsedSeconds }.thenByDescending { it.score })
            LeaderboardSortBy.DATE_DESC -> filtered.sortedByDescending { it.playedAt }
            LeaderboardSortBy.CATEGORY -> filtered.sortedWith(compareBy<GameHistoryEntry> { it.category }.thenByDescending { it.score })
        }

        filtered.take(100)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun setFilter(config: LeaderboardFilterConfig) {
        _filterConfig.value = config
    }

    private fun startOfToday(): Long {
        val now = System.currentTimeMillis()
        return now - (now % 86400000L) - (java.util.TimeZone.getDefault().rawOffset)
    }

    private fun startOfWeek(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun startOfMonth(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
