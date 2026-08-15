package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.DailyStreak
import com.LetterQuest.domain.repository.DailyChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyChallengeUIState(
    val isCompleted: Boolean = false,
    val wasWon: Boolean = false,
    val hintText: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    private val dailyChallengeRepository: DailyChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyChallengeUIState())
    val uiState: StateFlow<DailyChallengeUIState> = _uiState.asStateFlow()

    val streak: StateFlow<DailyStreak> = dailyChallengeRepository.observeStreak()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DailyStreak()
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            dailyChallengeRepository.getTodaysChallenge()
                .onSuccess { challenge ->
                    _uiState.value = DailyChallengeUIState(
                        isCompleted = challenge.isCompleted,
                        wasWon = challenge.wasWon,
                        // The word itself is never surfaced — only its hint, so the
                        // puzzle is not spoiled before play.
                        hintText = challenge.word.hint,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = DailyChallengeUIState(isLoading = false)
                }
        }
    }
}
