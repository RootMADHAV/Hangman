package com.LetterQuest.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.LetterQuest.domain.model.Achievement
import com.LetterQuest.domain.model.Difficulty
import com.LetterQuest.domain.model.GameHistoryEntry
import com.LetterQuest.domain.model.PlayerProfile
import com.LetterQuest.domain.repository.CloudSyncRepository
import com.LetterQuest.domain.repository.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CloudSyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus())
    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun uploadProfile(profile: PlayerProfile): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val data = hashMapOf<String, Any>(
            "playerId" to profile.playerId,
            "nickname" to profile.nickname,
            "username" to profile.username,
            "avatarId" to profile.avatarId,
            "totalGamesPlayed" to profile.totalGamesPlayed,
            "totalTokensEarned" to profile.totalTokensEarned,
            "createdAt" to profile.createdAt,
            "updatedAt" to profile.updatedAt
        )
        firestore.collection("user_profiles")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .await()
        updateStatus(isOnline = true, lastError = null)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Upload profile failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun downloadProfile(): Result<PlayerProfile?> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val snapshot = firestore.collection("user_profiles")
            .document(user.uid)
            .get()
            .await()
        if (snapshot.exists()) {
            val profile = PlayerProfile(
                playerId = snapshot.getString("playerId") ?: "",
                nickname = snapshot.getString("nickname") ?: "Player",
                username = snapshot.getString("username") ?: "",
                avatarId = snapshot.getString("avatarId") ?: "avatar_1",
                totalGamesPlayed = snapshot.getLong("totalGamesPlayed")?.toInt() ?: 0,
                totalTokensEarned = snapshot.getLong("totalTokensEarned")?.toInt() ?: 0,
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
            )
            updateStatus(isOnline = true, lastError = null)
            Result.success(profile)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Download profile failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun uploadGameHistory(entries: List<GameHistoryEntry>): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val batch = firestore.batch()
        val collection = firestore.collection("user_game_history").document(user.uid).collection("entries")
        for (entry in entries) {
            if (entry.uuid.isBlank()) continue
            val data = hashMapOf<String, Any>(
                "uuid" to entry.uuid,
                "word" to entry.word,
                "difficulty" to entry.difficulty.name,
                "won" to entry.won,
                "score" to entry.score,
                "sessionScore" to entry.sessionScore,
                "guessedLetters" to entry.guessedLetters.joinToString(","),
                "incorrectGuesses" to entry.incorrectGuesses.joinToString(","),
                "elapsedSeconds" to entry.elapsedSeconds,
                "playedAt" to entry.playedAt,
                "updatedAt" to entry.updatedAt,
                "category" to (entry.category ?: "")
            )
            val docRef = collection.document(entry.uuid)
            batch.set(docRef, data, SetOptions.merge())
        }
        batch.commit().await()
        updateStatus(isOnline = true, lastError = null)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Upload game history failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun downloadGameHistory(): Result<List<GameHistoryEntry>> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val snapshot = firestore.collection("user_game_history")
            .document(user.uid)
            .collection("entries")
            .get()
            .await()
        val entries = snapshot.documents.mapNotNull { doc ->
            try {
                GameHistoryEntry(
                    word = doc.getString("word") ?: return@mapNotNull null,
                    difficulty = Difficulty.valueOf(doc.getString("difficulty") ?: return@mapNotNull null),
                    won = doc.getBoolean("won") ?: false,
                    score = doc.getLong("score")?.toInt() ?: 0,
                    sessionScore = doc.getLong("sessionScore")?.toInt() ?: 0,
                    guessedLetters = (doc.getString("guessedLetters") ?: "").split(",").filter { it.isNotBlank() }.map { it.single() }.toSet(),
                    incorrectGuesses = (doc.getString("incorrectGuesses") ?: "").split(",").filter { it.isNotBlank() }.map { it.single() }.toSet(),
                    elapsedSeconds = doc.getLong("elapsedSeconds") ?: 0,
                    playedAt = doc.getLong("playedAt") ?: 0,
                    updatedAt = doc.getLong("updatedAt") ?: 0,
                    category = doc.getString("category")?.ifBlank { null }
                )
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed game history doc ${doc.id}", e)
                null
            }
        }
        updateStatus(isOnline = true, lastError = null)
        Result.success(entries)
    } catch (e: Exception) {
        Log.e(TAG, "Download game history failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun uploadLeaderboardScore(
        metric: String,
        value: Float,
        gamesPlayed: Int,
        gamesWon: Int,
        username: String,
        nickname: String,
        avatarId: String
    ): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = firestore.collection("leaderboards")
            .document(metric)
            .collection("entries")
            .document(user.uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentValue = snapshot.getDouble("value")?.toFloat() ?: 0f
            val currentGamesPlayed = snapshot.getLong("gamesPlayed")?.toInt() ?: 0
            val currentGamesWon = snapshot.getLong("gamesWon")?.toInt() ?: 0

            transaction.set(
                docRef,
                mapOf(
                    "value" to (currentValue + value),
                    "gamesPlayed" to (currentGamesPlayed + gamesPlayed),
                    "gamesWon" to (currentGamesWon + gamesWon),
                    "username" to username,
                    "nickname" to nickname,
                    "avatarId" to avatarId,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
        }.await()
        updateStatus(isOnline = true, lastError = null)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Upload leaderboard score failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun uploadAchievements(achievements: List<Achievement>): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val batch = firestore.batch()
        val collection = firestore.collection("user_achievements").document(user.uid).collection("entries")
        for (achievement in achievements) {
            val data = hashMapOf<String, Any>(
                "id" to achievement.id,
                "name" to achievement.name,
                "description" to achievement.description,
                "icon" to (achievement.icon ?: ""),
                "condition" to achievement.condition,
                "rewardTokens" to achievement.rewardTokens,
                "unlockedAt" to (achievement.unlockedAt ?: 0),
                "isUnlocked" to achievement.isUnlocked,
                "updatedAt" to (achievement.unlockedAt ?: 0)
            )
            val docRef = collection.document(achievement.id)
            batch.set(docRef, data, SetOptions.merge())
        }
        batch.commit().await()
        updateStatus(isOnline = true, lastError = null)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Upload achievements failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun downloadAchievements(): Result<List<Achievement>> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val snapshot = firestore.collection("user_achievements")
            .document(user.uid)
            .collection("entries")
            .get()
            .await()
        val achievements = snapshot.documents.mapNotNull { doc ->
            val isUnlocked = doc.getBoolean("isUnlocked") ?: false
            val unlockedAt = doc.getLong("unlockedAt")
            Achievement(
                id = doc.getString("id") ?: return@mapNotNull null,
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                icon = doc.getString("icon")?.ifBlank { null },
                condition = doc.getString("condition") ?: "",
                rewardTokens = doc.getLong("rewardTokens")?.toInt() ?: 0,
                unlockedAt = if (isUnlocked) (unlockedAt ?: 0) else null,
                isUnlocked = isUnlocked
            )
        }
        updateStatus(isOnline = true, lastError = null)
        Result.success(achievements)
    } catch (e: Exception) {
        Log.e(TAG, "Download achievements failed", e)
        updateStatus(isOnline = false, lastError = e.message)
        Result.failure(e)
    }

    override suspend fun deleteProfile(): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        firestore.collection("user_profiles").document(user.uid).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteGameHistory(): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val collection = firestore.collection("user_game_history").document(user.uid).collection("entries")
        val snapshot = collection.get().await()
        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAchievements(): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val collection = firestore.collection("user_achievements").document(user.uid).collection("entries")
        val snapshot = collection.get().await()
        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getLastSyncTimestamp(): Long? = try {
        val user = firebaseAuth.currentUser ?: return null
        val snapshot = firestore.collection("user_sync_meta").document(user.uid).get().await()
        snapshot.getLong("lastSyncAt")
    } catch (e: Exception) {
        null
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        try {
            val user = firebaseAuth.currentUser ?: return
            firestore.collection("user_sync_meta").document(user.uid)
                .set(mapOf("lastSyncAt" to timestamp))
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set last sync timestamp", e)
        }
    }

    private fun updateStatus(isOnline: Boolean, lastError: String?) {
        _syncStatus.value = SyncStatus(
            isOnline = isOnline,
            lastSyncAt = System.currentTimeMillis(),
            pendingUploads = 0,
            lastError = lastError
        )
    }

    companion object {
        private const val TAG = "CloudSyncRepository"
    }
}
