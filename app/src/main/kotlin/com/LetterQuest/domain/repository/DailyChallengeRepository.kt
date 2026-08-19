package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.DailyChallenge
import com.LetterQuest.domain.model.DailyStreak
import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    fun observeStreak(): Flow<DailyStreak>

    suspend fun getStreak(): Result<DailyStreak>

    suspend fun getTodaysChallenge(): Result<DailyChallenge>

    suspend fun recordCompletion(won: Boolean): Result<DailyStreak>

    suspend fun recordAttempt(): Result<Unit>

    suspend fun hasAdRetryAvailable(): Result<Boolean>

    suspend fun markAdRetryUsed(): Result<Unit>
}
