package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.WordCategory
import com.LetterQuest.domain.usecase.WordSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategorySelectUIState(
    val categories: List<WordCategory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategorySelectViewModel @Inject constructor(
    private val wordSelector: WordSelector
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategorySelectUIState())
    val uiState: StateFlow<CategorySelectUIState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            wordSelector.getCategories()
                .onSuccess { categories ->
                    _uiState.value = CategorySelectUIState(
                        categories = categories,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = CategorySelectUIState(
                        isLoading = false,
                        error = error.message ?: "Failed to load categories"
                    )
                }
        }
    }
}
