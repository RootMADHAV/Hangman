package com.LetterQuest.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.LetterQuest.domain.model.GlobalLeaderboardEntry
import com.LetterQuest.domain.model.LeaderboardMetric
import com.LetterQuest.domain.repository.LeaderboardRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    override fun observeLeaderboard(metric: LeaderboardMetric, limit: Int): Flow<List<GlobalLeaderboardEntry>> {
        return callbackFlow {
            val listenerRegistration = firestore.collection("leaderboards")
                .document(metric.name)
                .collection("entries")
                .orderBy("value", Query.Direction.DESCENDING)
                .orderBy("updatedAt", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Leaderboard listener error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val entries = snapshot?.documents?.mapIndexed { index, doc ->
                        GlobalLeaderboardEntry(
                            userId = doc.id,
                            username = doc.getString("username") ?: "",
                            nickname = doc.getString("nickname") ?: "Player",
                            avatarId = doc.getString("avatarId") ?: "avatar_1",
                            value = doc.getDouble("value")?.toFloat() ?: 0f,
                            gamesPlayed = doc.getLong("gamesPlayed")?.toInt() ?: 0,
                            gamesWon = doc.getLong("gamesWon")?.toInt() ?: 0,
                            updatedAt = doc.getLong("updatedAt") ?: 0,
                            rank = index + 1
                        )
                    } ?: emptyList()
                    trySend(entries)
                }
            awaitClose { listenerRegistration.remove() }
        }.map { entries ->
            entries.map { entry ->
                if (entry.rank == 0) {
                    entry.copy(rank = entries.indexOf(entry) + 1)
                } else {
                    entry
                }
            }
        }
    }

    override fun observeCurrentUserEntry(metric: LeaderboardMetric, userId: String): Flow<GlobalLeaderboardEntry?> {
        return callbackFlow {
            val listenerRegistration = firestore.collection("leaderboards")
                .document(metric.name)
                .collection("entries")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Current user leaderboard listener error", error)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val entry = snapshot?.let { doc ->
                        if (doc.exists()) {
                            GlobalLeaderboardEntry(
                                userId = doc.id,
                                username = doc.getString("username") ?: "",
                                nickname = doc.getString("nickname") ?: "Player",
                                avatarId = doc.getString("avatarId") ?: "avatar_1",
                                value = doc.getDouble("value")?.toFloat() ?: 0f,
                                gamesPlayed = doc.getLong("gamesPlayed")?.toInt() ?: 0,
                                gamesWon = doc.getLong("gamesWon")?.toInt() ?: 0,
                                updatedAt = doc.getLong("updatedAt") ?: 0
                            )
                        } else {
                            null
                        }
                    }
                    trySend(entry)
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    override suspend fun getCurrentUserRank(metric: LeaderboardMetric, userId: String): Result<GlobalLeaderboardEntry?> {
        return try {
            val doc = firestore.collection("leaderboards")
                .document(metric.name)
                .collection("entries")
                .document(userId)
                .get()
                .await()
            if (!doc.exists()) {
                Result.success(null)
            } else {
                val entry = GlobalLeaderboardEntry(
                    userId = doc.id,
                    username = doc.getString("username") ?: "",
                    nickname = doc.getString("nickname") ?: "Player",
                    avatarId = doc.getString("avatarId") ?: "avatar_1",
                    value = doc.getDouble("value")?.toFloat() ?: 0f,
                    gamesPlayed = doc.getLong("gamesPlayed")?.toInt() ?: 0,
                    gamesWon = doc.getLong("gamesWon")?.toInt() ?: 0,
                    updatedAt = doc.getLong("updatedAt") ?: 0
                )
                Result.success(entry)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitScore(
        metric: LeaderboardMetric,
        value: Float,
        gamesPlayed: Int,
        gamesWon: Int,
        username: String,
        nickname: String,
        avatarId: String
    ): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = firestore.collection("leaderboards")
            .document(metric.name)
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
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Submit score failed", e)
        Result.failure(e)
    }

    companion object {
        private const val TAG = "LeaderboardRepository"
    }
}
