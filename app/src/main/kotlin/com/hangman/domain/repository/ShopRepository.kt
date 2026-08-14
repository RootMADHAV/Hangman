package com.hangman.domain.repository

import com.hangman.domain.model.ShopItem
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    fun observeOwnedItems(): Flow<Set<ShopItem>>

    suspend fun getOwnedItems(): Result<Set<ShopItem>>

    /**
     * Records [item] as activated for the current game.
     * Power-ups are single-use per game and are cleared when a new game starts.
     */
    suspend fun markPurchased(item: ShopItem): Result<Unit>

    /**
     * Clears all activated power-ups at the start of a new game.
     */
    suspend fun clearActivatedPerks(): Result<Unit>
}
