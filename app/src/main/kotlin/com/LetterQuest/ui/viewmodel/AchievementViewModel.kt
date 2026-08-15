package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Placeholder Hilt ViewModel. The Achievements screen currently sources its
 * state from [HomeViewModel]; the richer unlock-flow binding is a follow-up.
 */
@HiltViewModel
class AchievementViewModel @Inject constructor() : ViewModel()
