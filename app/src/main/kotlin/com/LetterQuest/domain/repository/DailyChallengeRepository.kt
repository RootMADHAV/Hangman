package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.DailyChallenge
import com.LetterQuest.domain.model.DailyStreak
import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    fun observeStreak(): Flow<DailyStreak>

    suspend fun getStreak(): Result<DailyStreak>

    /** Today's puzzle, with its completion state resolved from storage. */
    suspend fun getTodaysChallenge(): Result<DailyChallenge>

    /**
     * Marks today complete and advances the streak. Recording the same day twice is a
     * no-op so a replay cannot inflate the streak.
     */
    suspend fun recordCompletion(won: Boolean): Result<DailyStreak>
}
