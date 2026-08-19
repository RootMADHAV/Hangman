package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.GlobalLeaderboardEntry
import com.LetterQuest.domain.model.LeaderboardMetric
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedMetric = MutableStateFlow(LeaderboardMetric.TOTAL_SCORE)
    val selectedMetric: StateFlow<LeaderboardMetric> = _selectedMetric.asStateFlow()

    val leaderboard: StateFlow<List<GlobalLeaderboardEntry>> = _selectedMetric
        .flatMapConcat { metric ->
            leaderboardRepository.observeLeaderboard(metric)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val currentUserEntry: StateFlow<GlobalLeaderboardEntry?> = _selectedMetric
        .flatMapConcat { metric ->
            authRepository.currentUser
                .flatMapConcat { authState ->
                    val userId = (authState as? AuthState.Authenticated)?.uid
                    if (userId != null) {
                        leaderboardRepository.observeCurrentUserEntry(metric, userId)
                    } else {
                        kotlinx.coroutines.flow.flowOf(null)
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setMetric(metric: LeaderboardMetric) {
        _selectedMetric.value = metric
    }
}
