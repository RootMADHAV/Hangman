package com.LetterQuest.domain.repository

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<AuthState>

    val profile: Flow<PlayerProfile?>

    suspend fun signInWithGoogle(idToken: String): AuthResult<Unit>

    suspend fun linkGuestToGoogle(idToken: String): AuthResult<Unit>

    suspend fun signInWithEmail(email: String, password: String): AuthResult<Unit>

    suspend fun signUpWithEmail(email: String, password: String): AuthResult<Unit>

    suspend fun signInAsGuest(): AuthResult<Unit>

    suspend fun linkGuestToEmail(email: String, password: String): AuthResult<Unit>

    suspend fun signOut(): AuthResult<Unit>

    suspend fun resetLocalData(): AuthResult<Unit>

    suspend fun sendEmailVerification(): AuthResult<Unit>

    suspend fun reloadUser(): AuthResult<Unit>

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>

    suspend fun backupUserData(): AuthResult<Unit>

    suspend fun restoreUserData(): AuthResult<Unit>

    suspend fun updateNickname(nickname: String): AuthResult<Unit>

    suspend fun updateUsername(username: String): AuthResult<Unit>

    suspend fun updateAvatar(avatarId: String): AuthResult<Unit>

    suspend fun importProfile(profile: PlayerProfile): AuthResult<Unit>
}
