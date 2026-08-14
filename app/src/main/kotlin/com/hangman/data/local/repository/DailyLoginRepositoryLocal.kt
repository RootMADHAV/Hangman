package com.hangman.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hangman.domain.model.DailyLoginReward
import com.hangman.domain.repository.DailyLoginRepository
import com.hangman.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DailyLoginRepositoryLocal @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val tokenRepository: TokenRepository
) : DailyLoginRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun observeDailyLoginState(): Flow<DailyLoginReward> =
        dataStore.data.map { preferences ->
            calculateDailyLoginState(preferences)
        }

    override suspend fun getDailyLoginState(): Result<DailyLoginReward> {
        return try {
            Result.success(observeDailyLoginState().first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun claimDailyReward(): Result<Int> {
        return try {
            val state = getDailyLoginState().getOrThrow()
            if (!state.canClaim) {
                return Result.failure(IllegalStateException("Daily reward already claimed today"))
            }

            val today = LocalDate.now().format(dateFormatter)
            val newStreak = state.currentStreak + 1
            val reward = calculateReward(newStreak)

            dataStore.edit { preferences ->
                preferences[LAST_CLAIMED_DATE] = today
                preferences[CURRENT_STREAK] = newStreak
            }

            tokenRepository.earnTokens(reward)
            Result.success(reward)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDailyLoginState(preferences: Preferences): DailyLoginReward {
        val lastClaimed = preferences[LAST_CLAIMED_DATE]
        val streak = preferences[CURRENT_STREAK] ?: 0
        val today = LocalDate.now().format(dateFormatter)

        val canClaim = lastClaimed != today
        val currentStreak = if (canClaim && lastClaimed != null) {
            val lastDate = LocalDate.parse(lastClaimed, dateFormatter)
            val daysSinceLastClaim = java.time.temporal.ChronoUnit.DAYS.between(lastDate, LocalDate.now())
            if (daysSinceLastClaim == 1L) streak else 0
        } else {
            streak
        }

        val reward = calculateReward(currentStreak + 1)

        return DailyLoginReward(
            lastClaimedDateKey = lastClaimed,
            currentStreak = currentStreak,
            canClaim = canClaim,
            tokensReward = reward
        )
    }

    private fun calculateReward(streak: Int): Int = DailyLoginReward.rewardForStreak(streak)

    private companion object {
        val LAST_CLAIMED_DATE = stringPreferencesKey("daily_login_last_claimed")
        val CURRENT_STREAK = intPreferencesKey("daily_login_streak")
    }
}
