package com.hangman.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hangman.domain.model.Achievement
import com.hangman.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUIState(
    val isVisible: Boolean = false,
    val achievement: Achievement? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _notificationState = MutableStateFlow(NotificationUIState())
    val notificationState: StateFlow<NotificationUIState> = _notificationState.asStateFlow()

    /** Achievements already shown this app session — prevents the per-word re-popup. */
    private val shownAchievementIds = mutableSetOf<String>()

    fun checkAndShowNotification() {
        viewModelScope.launch {
            val recentUnlock = achievementRepository.getUnlockedAchievements()
                .getOrNull()
                ?.maxByOrNull { it.unlockedAt ?: 0L }

            if (recentUnlock != null && recentUnlock.unlockedAt != null) {
                // Only surface each achievement once per session. If the achievement
                // was just unlocked in this exact moment (last few seconds) allow a
                // repeat popup; otherwise dedupe by id so wins don't replay "First
                // Victory" over and over.
                val id = recentUnlock.id
                if (id in shownAchievementIds) return@launch
                shownAchievementIds += id

                _notificationState.emit(
                    NotificationUIState(
                        isVisible = true,
                        achievement = recentUnlock
                    )
                )
            }
        }
    }

    fun dismissNotification() {
        viewModelScope.launch {
            _notificationState.emit(NotificationUIState(isVisible = false, achievement = null))
        }
    }
}
