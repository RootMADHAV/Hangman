package com.LetterQuest.data.local.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncQueue @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cloudSyncRepository: com.LetterQuest.domain.repository.CloudSyncRepository
) {
    suspend fun enqueueUploadProfile(profile: PlayerProfile) {
        enqueue(SyncOperation.UploadProfile(profile))
    }

    suspend fun enqueueUploadGameHistory(entries: List<GameHistoryEntry>) {
        enqueue(SyncOperation.UploadGameHistory(entries))
    }

    suspend fun enqueueUploadAchievements(achievements: List<Achievement>) {
        enqueue(SyncOperation.UploadAchievements(achievements))
    }

    suspend fun drain() {
        val operations = getPendingOperations()
        if (operations.isEmpty()) return

        var lastError: String? = null
        for (op in operations) {
            when (op) {
                is SyncOperation.UploadProfile -> {
                    cloudSyncRepository.uploadProfile(op.profile).onFailure { lastError = it.message }
                }
                is SyncOperation.UploadGameHistory -> {
                    cloudSyncRepository.uploadGameHistory(op.entries).onFailure { lastError = it.message }
                }
                is SyncOperation.UploadAchievements -> {
                    cloudSyncRepository.uploadAchievements(op.achievements).onFailure { lastError = it.message }
                }
            }
        }
        if (lastError == null) {
            clearQueue()
        }
    }

    fun observePendingCount(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[PENDING_QUEUE]?.let { raw ->
            try { JSONArray(raw).length() } catch (e: Exception) { 0 }
        } ?: 0
    }

    private suspend fun enqueue(operation: SyncOperation) {
        dataStore.edit { prefs ->
            val current = prefs[PENDING_QUEUE] ?: "[]"
            val array = JSONArray(current)
            val obj = JSONObject().apply {
                put("type", operation.type)
                put("payload", operation.toJson())
            }
            array.put(obj)
            prefs[PENDING_QUEUE] = array.toString()
        }
    }

    private suspend fun getPendingOperations(): List<SyncOperation> {
        val prefs = dataStore.data.first()
        val raw = prefs[PENDING_QUEUE] ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<SyncOperation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = obj.getString("type")
                val payload = obj.getJSONObject("payload")
                when (type) {
                    "upload_profile" -> SyncOperation.UploadProfile.fromJson(payload)?.let { result.add(it) }
                    "upload_history" -> SyncOperation.UploadGameHistory.fromJson(payload)?.let { result.add(it) }
                    "upload_achievements" -> SyncOperation.UploadAchievements.fromJson(payload)?.let { result.add(it) }
                }
            }
            result
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse sync queue, clearing", e)
            clearQueue()
            emptyList()
        }
    }

    private suspend fun clearQueue() {
        dataStore.edit { prefs -> prefs.remove(PENDING_QUEUE) }
    }

    private companion object {
        val PENDING_QUEUE = stringPreferencesKey("sync_pending_queue")
        const val TAG = "SyncQueue"
    }
}

sealed class SyncOperation {
    abstract val type: String
    abstract fun toJson(): JSONObject

    data class UploadProfile(val profile: PlayerProfile) : SyncOperation() {
        override val type = "upload_profile"
        override fun toJson(): JSONObject = JSONObject().apply {
            put("playerId", profile.playerId)
            put("nickname", profile.nickname)
            put("username", profile.username)
            put("avatarId", profile.avatarId)
            put("totalGamesPlayed", profile.totalGamesPlayed)
            put("totalTokensEarned", profile.totalTokensEarned)
            put("createdAt", profile.createdAt)
            put("updatedAt", profile.updatedAt)
        }
        companion object {
            fun fromJson(json: JSONObject): UploadProfile? = try {
                UploadProfile(
                    profile = PlayerProfile(
                        playerId = json.getString("playerId"),
                        nickname = json.getString("nickname"),
                        username = json.getString("username"),
                        avatarId = json.getString("avatarId"),
                        totalGamesPlayed = json.getInt("totalGamesPlayed"),
                        totalTokensEarned = json.getInt("totalTokensEarned"),
                        createdAt = json.getLong("createdAt"),
                        updatedAt = json.getLong("updatedAt")
                    )
                )
            } catch (e: Exception) { null }
        }
    }

    data class UploadGameHistory(val entries: List<GameHistoryEntry>) : SyncOperation() {
        override val type = "upload_history"
        override fun toJson(): JSONObject = JSONObject().apply {
            put("count", entries.size)
            val arr = JSONArray()
            for (e in entries) {
                arr.put(JSONObject().apply {
                    put("uuid", e.uuid)
                    put("word", e.word)
                    put("difficulty", e.difficulty.name)
                    put("won", e.won)
                    put("score", e.score)
                    put("sessionScore", e.sessionScore)
                    put("guessedLetters", e.guessedLetters.joinToString(","))
                    put("incorrectGuesses", e.incorrectGuesses.joinToString(","))
                    put("elapsedSeconds", e.elapsedSeconds)
                    put("playedAt", e.playedAt)
                    put("updatedAt", e.updatedAt)
                    put("category", e.category ?: "")
                })
            }
            put("entries", arr)
        }
        companion object {
            fun fromJson(json: JSONObject): UploadGameHistory? = try {
                val arr = json.getJSONArray("entries")
                val entries = mutableListOf<GameHistoryEntry>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    entries.add(
                        GameHistoryEntry(
                            word = obj.getString("word"),
                            difficulty = Difficulty.valueOf(obj.getString("difficulty")),
                            won = obj.getBoolean("won"),
                            score = obj.getInt("score"),
                            sessionScore = obj.getInt("sessionScore"),
                            guessedLetters = (obj.getString("guessedLetters")).split(",").filter { it.isNotBlank() }.map { it.single() }.toSet(),
                            incorrectGuesses = (obj.getString("incorrectGuesses")).split(",").filter { it.isNotBlank() }.map { it.single() }.toSet(),
                            elapsedSeconds = obj.getLong("elapsedSeconds"),
                            playedAt = obj.getLong("playedAt"),
                            updatedAt = obj.getLong("updatedAt"),
                            category = obj.getString("category").ifBlank { null }
                        )
                    )
                }
                UploadGameHistory(entries)
            } catch (e: Exception) { null }
        }
    }

    data class UploadAchievements(val achievements: List<Achievement>) : SyncOperation() {
        override val type = "upload_achievements"
        override fun toJson(): JSONObject = JSONObject().apply {
            val arr = JSONArray()
            for (a in achievements) {
                arr.put(JSONObject().apply {
                    put("id", a.id)
                    put("name", a.name)
                    put("description", a.description)
                    put("icon", a.icon ?: "")
                    put("unlockedAt", a.unlockedAt ?: 0)
                    put("isUnlocked", a.isUnlocked)
                })
            }
            put("achievements", arr)
        }
        companion object {
            fun fromJson(json: JSONObject): UploadAchievements? = try {
                val arr = json.getJSONArray("achievements")
                val achievements = mutableListOf<Achievement>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    achievements.add(
                        Achievement(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.getString("description"),
                            icon = obj.getString("icon").ifBlank { null },
                            unlockedAt = if (obj.getBoolean("isUnlocked")) obj.getLong("unlockedAt") else null,
                            isUnlocked = obj.getBoolean("isUnlocked")
                        )
                    )
                }
                UploadAchievements(achievements)
            } catch (e: Exception) { null }
        }
    }
}
