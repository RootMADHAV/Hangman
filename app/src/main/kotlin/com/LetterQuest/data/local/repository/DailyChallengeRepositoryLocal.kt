package com.LetterQuest.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.LetterQuest.domain.model.DailyChallenge
import com.LetterQuest.domain.model.DailyStreak
import com.LetterQuest.domain.repository.DailyChallengeRepository
import com.LetterQuest.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class DailyChallengeRepositoryLocal @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val wordRepository: WordRepository,
    private val clock: Clock
) : DailyChallengeRepository {

    override fun observeStreak(): Flow<DailyStreak> =
        dataStore.data.map { it.toStreak() }

    override suspend fun getStreak(): Result<DailyStreak> {
        return try {
            Result.success(observeStreak().first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodaysChallenge(): Result<DailyChallenge> {
        return try {
            val words = wordRepository.getAllWords().getOrThrow()
            if (words.isEmpty()) {
                return Result.failure(IllegalStateException("Word catalog is empty"))
            }

            val todayKey = DailyChallenge.dateKeyFor(LocalDate.now(clock))
            val word = words[DailyChallenge.indexFor(todayKey, words.size)]
            val preferences = dataStore.data.first()

            val completedKey = preferences[LAST_COMPLETED_DATE]
            val isCompleted = completedKey == todayKey

            Result.success(
                DailyChallenge(
                    dateKey = todayKey,
                    word = word,
                    isCompleted = isCompleted,
                    wasWon = isCompleted && (preferences[LAST_RESULT_WON] ?: false)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordCompletion(won: Boolean): Result<DailyStreak> {
        return try {
            val today = LocalDate.now(clock)
            val todayKey = DailyChallenge.dateKeyFor(today)

            val existing = dataStore.data.first()
            if (existing[LAST_COMPLETED_DATE] == todayKey) {
                return Result.success(existing.toStreak())
            }

            val yesterdayKey = DailyChallenge.dateKeyFor(today.minusDays(1))
            dataStore.edit { preferences ->
                val previousStreak = preferences[CURRENT_STREAK] ?: 0
                val newCurrent = if (preferences[LAST_COMPLETED_DATE] == yesterdayKey) {
                    previousStreak + 1
                } else {
                    1
                }

                preferences[CURRENT_STREAK] = newCurrent
                preferences[LONGEST_STREAK] = maxOf(preferences[LONGEST_STREAK] ?: 0, newCurrent)
                preferences[LAST_COMPLETED_DATE] = todayKey
                preferences[LAST_RESULT_WON] = won
            }

            Result.success(dataStore.data.first().toStreak())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Preferences.toStreak(): DailyStreak {
        val current = this[CURRENT_STREAK] ?: 0
        return DailyStreak(
            current = current,
            longest = maxOf(this[LONGEST_STREAK] ?: 0, current),
            lastCompletedDateKey = this[LAST_COMPLETED_DATE]
        )
    }

    private companion object {
        val CURRENT_STREAK = intPreferencesKey("daily_current_streak")
        val LONGEST_STREAK = intPreferencesKey("daily_longest_streak")
        val LAST_COMPLETED_DATE = stringPreferencesKey("daily_last_completed_date")
        val LAST_RESULT_WON = booleanPreferencesKey("daily_last_result_won")
    }
}
