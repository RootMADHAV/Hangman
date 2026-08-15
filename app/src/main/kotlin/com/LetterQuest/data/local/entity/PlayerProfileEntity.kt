package com.LetterQuest.data.local.entity

data class PlayerProfileEntity(
    val id: String,
    val nickname: String,
    val avatarId: String,
    val totalGamesPlayed: Int,
    val totalTokensEarned: Int,
    val createdAt: Long,
    val lastPlayedAt: Long,
    val authProvider: String,
    val firebaseUid: String?,
    val email: String?
) {
    companion object {
        const val AUTH_PROVIDER_GUEST = "guest"
        const val AUTH_PROVIDER_GOOGLE = "google"
        const val AUTH_PROVIDER_EMAIL = "email"
    }
}
