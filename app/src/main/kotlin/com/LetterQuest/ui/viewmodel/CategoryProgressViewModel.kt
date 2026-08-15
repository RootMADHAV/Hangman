package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.CategoryProgress
import com.LetterQuest.domain.usecase.CategoryProgressCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryProgressUIState(
    val progress: List<CategoryProgress> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val masteredCount: Int
        get() = progress.count { it.isMastered }

    val playedCount: Int
        get() = progress.count { !it.isUnplayed }
}

@HiltViewModel
class CategoryProgressViewModel @Inject constructor(
    private val categoryProgressCalculator: CategoryProgressCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryProgressUIState())
    val uiState: StateFlow<CategoryProgressUIState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            categoryProgressCalculator.calculate()
                .onSuccess { progress ->
                    _uiState.value = CategoryProgressUIState(
                        // Most-played first so the player's active categories lead.
                        progress = progress.sortedByDescending { it.gamesPlayed },
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = CategoryProgressUIState(
                        isLoading = false,
                        error = error.message ?: "Failed to load category progress"
                    )
                }
        }
    }
}
