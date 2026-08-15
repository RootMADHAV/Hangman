package com.LetterQuest.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import com.LetterQuest.domain.model.UserTokens
import com.LetterQuest.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

class TokenRepositoryLocal @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : TokenRepository {

    private val _tokens = MutableStateFlow(UserTokens(UserTokens.STARTING_BALANCE))
    private val encPrefs by lazy { getEncryptedPreferences() }
    private val TAG = "TokenRepository"

    init {
        val balance = encPrefs.getInt(TOKEN_BALANCE_KEY, UserTokens.STARTING_BALANCE)
        val timestamp = encPrefs.getLong(TOKEN_TIMESTAMP_KEY, 0L)
        val checksum = encPrefs.getString(TOKEN_CHECKSUM_KEY, null)
        val valid = verifyChecksum(balance, timestamp, checksum)
        if (!valid) {
            android.util.Log.w(TAG, "Token checksum invalid on init; resetting to default balance")
            resetToDefault()
        } else {
            _tokens.value = UserTokens(balance)
        }
    }

    private fun getEncryptedPreferences() = EncryptedSharedPreferences.create(
        context,
        "token_storage",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Returns the per-device HMAC key, generating and persisting it on first call.
     * Stored alongside the token data in EncryptedSharedPreferences so it never
     * leaves the device unencrypted.
     */
    private fun getOrCreateHmacKey(): ByteArray {
        val stored = encPrefs.getString(TOKEN_HMAC_SECRET, null)
        if (stored != null) return Base64.getDecoder().decode(stored)
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        encPrefs.edit().putString(TOKEN_HMAC_SECRET, Base64.getEncoder().encodeToString(key)).apply()
        return key
    }

    private fun computeChecksum(balance: Int, timestamp: Long): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(getOrCreateHmacKey(), "HmacSHA256"))
        return Base64.getEncoder().encodeToString(mac.doFinal("$balance:$timestamp".toByteArray()))
    }

    private fun verifyChecksum(balance: Int, timestamp: Long, expected: String?): Boolean {
        if (expected == null) {
            return false
        }
        return computeChecksum(balance, timestamp) == expected
    }

    private fun resetToDefault() {
        val newTimestamp = System.currentTimeMillis()
        encPrefs.edit().apply {
            putInt(TOKEN_BALANCE_KEY, UserTokens.STARTING_BALANCE)
            putLong(TOKEN_TIMESTAMP_KEY, newTimestamp)
            putString(TOKEN_CHECKSUM_KEY, computeChecksum(UserTokens.STARTING_BALANCE, newTimestamp))
            apply()
        }
        _tokens.value = UserTokens(UserTokens.STARTING_BALANCE)
    }

    override fun observeTokens(): Flow<UserTokens> = _tokens.asStateFlow()

    override suspend fun getTokens(): Result<UserTokens> {
        return try {
            Result.success(_tokens.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun spendTokens(amount: Int): Result<UserTokens> {
        require(amount >= 0) { "Spend amount cannot be negative, was $amount" }
        return try {
            val current = encPrefs.getInt(TOKEN_BALANCE_KEY, UserTokens.STARTING_BALANCE)
            val timestamp = encPrefs.getLong(TOKEN_TIMESTAMP_KEY, 0L)
            val checksum = encPrefs.getString(TOKEN_CHECKSUM_KEY, null)
            if (!verifyChecksum(current, timestamp, checksum)) {
                android.util.Log.w(TAG, "Token checksum verification failed during spend; resetting to safe default")
                resetToDefault()
                return Result.failure(IllegalStateException("Token integrity check failed"))
            }
            if (current < amount) {
                return Result.failure(IllegalStateException("Not enough tokens to spend $amount"))
            }
            val newBalance = current - amount
            val newTimestamp = System.currentTimeMillis()
            encPrefs.edit().apply {
                putInt(TOKEN_BALANCE_KEY, newBalance)
                putLong(TOKEN_TIMESTAMP_KEY, newTimestamp)
                putString(TOKEN_CHECKSUM_KEY, computeChecksum(newBalance, newTimestamp))
                apply()
            }
            val updated = UserTokens(newBalance)
            _tokens.value = updated
            dataStore.edit { it[SENTINEL_KEY] = newTimestamp }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun earnTokens(amount: Int): Result<UserTokens> {
        require(amount >= 0) { "Earn amount cannot be negative, was $amount" }
        return try {
            val current = encPrefs.getInt(TOKEN_BALANCE_KEY, UserTokens.STARTING_BALANCE)
            val timestamp = encPrefs.getLong(TOKEN_TIMESTAMP_KEY, 0L)
            val checksum = encPrefs.getString(TOKEN_CHECKSUM_KEY, null)
            if (!verifyChecksum(current, timestamp, checksum)) {
                android.util.Log.w(TAG, "Token checksum verification failed during earn; resetting to safe default")
                resetToDefault()
                return Result.failure(IllegalStateException("Token integrity check failed"))
            }
            val newBalance = current + amount
            val newTimestamp = System.currentTimeMillis()
            encPrefs.edit().apply {
                putInt(TOKEN_BALANCE_KEY, newBalance)
                putLong(TOKEN_TIMESTAMP_KEY, newTimestamp)
                putString(TOKEN_CHECKSUM_KEY, computeChecksum(newBalance, newTimestamp))
                apply()
            }
            val updated = UserTokens(newBalance)
            _tokens.value = updated
            dataStore.edit { it[SENTINEL_KEY] = newTimestamp }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reset() {
        resetToDefault()
    }

    private companion object {
        const val TOKEN_BALANCE_KEY = "token_balance"
        const val TOKEN_TIMESTAMP_KEY = "token_timestamp"
        const val TOKEN_CHECKSUM_KEY = "token_checksum"
        const val TOKEN_HMAC_SECRET = "token_hmac_secret"
        val SENTINEL_KEY = longPreferencesKey("token_sentinel")
    }
}
