package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.UserTokens
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    fun observeTokens(): Flow<UserTokens>

    suspend fun getTokens(): Result<UserTokens>

    /**
     * Deducts [amount] and returns the new balance, or fails if the player cannot
     * afford it. Callers must treat failure as "hint not applied".
     */
    suspend fun spendTokens(amount: Int): Result<UserTokens>

    suspend fun earnTokens(amount: Int): Result<UserTokens>

    suspend fun reset()
}
