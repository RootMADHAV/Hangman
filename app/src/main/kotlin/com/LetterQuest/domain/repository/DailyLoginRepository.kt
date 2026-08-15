package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.DailyLoginReward
import kotlinx.coroutines.flow.Flow

interface DailyLoginRepository {
    /**
     * Observes the current daily login state, recalculated whenever the date changes.
     */
    fun observeDailyLoginState(): Flow<DailyLoginReward>

    /**
     * Gets the current daily login state.
     */
    suspend fun getDailyLoginState(): Result<DailyLoginReward>

    /**
     * Claims today's reward if available. Returns the tokens awarded or fails if already claimed.
     */
    suspend fun claimDailyReward(): Result<Int>
}
