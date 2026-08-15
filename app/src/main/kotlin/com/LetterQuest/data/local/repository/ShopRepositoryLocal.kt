package com.LetterQuest.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.LetterQuest.domain.model.ShopItem
import com.LetterQuest.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShopRepositoryLocal @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ShopRepository {

    override fun observeOwnedItems(): Flow<Set<ShopItem>> =
        dataStore.data.map { preferences ->
            preferences[ACTIVE_PERKS]
                .orEmpty()
                .mapNotNull { ShopItem.fromId(it) }
                .toSet()
        }

    override suspend fun getOwnedItems(): Result<Set<ShopItem>> {
        return try {
            Result.success(observeOwnedItems().first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Activates the [item] for the current game. Re-purchasing an already active item fails. */
    override suspend fun markPurchased(item: ShopItem): Result<Unit> {
        return try {
            var alreadyOwned = false
            dataStore.edit { preferences ->
                val active = preferences[ACTIVE_PERKS].orEmpty()
                if (item.id in active) {
                    alreadyOwned = true
                } else {
                    preferences[ACTIVE_PERKS] = active + item.id
                }
            }
            if (alreadyOwned) {
                Result.failure(IllegalStateException("${item.displayName} is already active for this game"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Clears all activated perks at the start of a new game so they must be re-purchased. */
    override suspend fun clearActivatedPerks(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[ACTIVE_PERKS] = emptySet()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        val ACTIVE_PERKS = stringSetPreferencesKey("shop_active_perks")
    }
}
