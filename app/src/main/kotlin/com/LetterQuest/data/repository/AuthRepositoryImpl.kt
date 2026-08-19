package com.LetterQuest.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.LetterQuest.data.local.entity.PlayerProfileEntity
import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.model.PlayerProfile
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.repository.TokenRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>,
    private val tokenRepository: TokenRepository
) : AuthRepository {

    override val currentUser: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(mapFirebaseUserToAuthState(auth.currentUser))
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val profile: Flow<PlayerProfile?> = dataStore.data.map { prefs ->
        if (prefs.contains(KEY_NICKNAME)) {
            PlayerProfile(
                playerId = "local_profile",
                nickname = prefs[KEY_NICKNAME] ?: "Player",
                username = prefs[KEY_USERNAME] ?: "",
                avatarId = prefs[KEY_AVATAR_ID] ?: "avatar_1",
                totalGamesPlayed = prefs[KEY_TOTAL_GAMES] ?: 0,
                totalTokensEarned = prefs[KEY_TOTAL_TOKENS] ?: 0,
                createdAt = (prefs[KEY_CREATED_AT] ?: 0).toLong(),
                updatedAt = (prefs[KEY_UPDATED_AT] ?: 0).toLong()
            )
        } else {
            null
        }
    }

    private suspend fun getProfileEntity(): PlayerProfileEntity? {
        val prefs = dataStore.data.first()
        return if (prefs.contains(KEY_NICKNAME)) {
            PlayerProfileEntity(
                id = "local_profile",
                nickname = prefs[KEY_NICKNAME] ?: "Player",
                username = prefs[KEY_USERNAME] ?: "",
                avatarId = prefs[KEY_AVATAR_ID] ?: "avatar_1",
                totalGamesPlayed = prefs[KEY_TOTAL_GAMES] ?: 0,
                totalTokensEarned = prefs[KEY_TOTAL_TOKENS] ?: 0,
                createdAt = (prefs[KEY_CREATED_AT] ?: 0).toLong(),
                updatedAt = (prefs[KEY_UPDATED_AT] ?: 0).toLong(),
                authProvider = prefs[KEY_AUTH_PROVIDER] ?: PlayerProfileEntity.AUTH_PROVIDER_GUEST,
                firebaseUid = prefs[KEY_FIREBASE_UID]?.ifEmpty { null },
                email = prefs[KEY_EMAIL]?.ifEmpty { null }
            )
        } else {
            null
        }
    }

    private fun mapFirebaseUserToAuthState(user: FirebaseUser?): AuthState {
        return if (user != null) {
            val isGuest = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID || it.providerId == EmailAuthProvider.PROVIDER_ID }.not()
            AuthState.Authenticated(
                uid = user.uid,
                displayName = user.displayName,
                email = user.email,
                isGuest = isGuest,
                isEmailVerified = user.isEmailVerified,
                username = ""
            )
        } else {
            AuthState.Unauthenticated
        }
    }

    private val KEY_NICKNAME = stringPreferencesKey("profile_nickname")
    private val KEY_USERNAME = stringPreferencesKey("profile_username")
    private val KEY_AVATAR_ID = stringPreferencesKey("profile_avatar_id")
    private val KEY_TOTAL_GAMES = intPreferencesKey("profile_total_games")
    private val KEY_TOTAL_TOKENS = intPreferencesKey("profile_total_tokens")
    private val KEY_CREATED_AT = intPreferencesKey("profile_created_at")
    private val KEY_UPDATED_AT = intPreferencesKey("profile_updated_at")
    private val KEY_AUTH_PROVIDER = stringPreferencesKey("profile_auth_provider")
    private val KEY_FIREBASE_UID = stringPreferencesKey("profile_firebase_uid")
    private val KEY_EMAIL = stringPreferencesKey("profile_email")

    private suspend fun saveProfile(profile: PlayerProfileEntity) {
        dataStore.edit { prefs ->
            prefs[KEY_NICKNAME] = profile.nickname
            prefs[KEY_USERNAME] = profile.username
            prefs[KEY_AVATAR_ID] = profile.avatarId
            prefs[KEY_TOTAL_GAMES] = profile.totalGamesPlayed
            prefs[KEY_TOTAL_TOKENS] = profile.totalTokensEarned
            prefs[KEY_CREATED_AT] = profile.createdAt.toInt()
            prefs[KEY_UPDATED_AT] = profile.updatedAt.toInt()
            prefs[KEY_AUTH_PROVIDER] = profile.authProvider
            prefs[KEY_FIREBASE_UID] = profile.firebaseUid ?: ""
            prefs[KEY_EMAIL] = profile.email ?: ""
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser
            val result = if (currentUser != null && currentUser.isAnonymous) {
                backupUserData()
                currentUser.linkWithCredential(credential).await()
            } else {
                firebaseAuth.signInWithCredential(credential).await()
            }
            result.user?.let { user ->
                val existing = getProfileEntity()
                val profileEntity = PlayerProfileEntity(
                    id = "local_profile",
                    nickname = user.displayName ?: "Player",
                    username = existing?.username ?: "",
                    avatarId = existing?.avatarId ?: "avatar_1",
                    totalGamesPlayed = existing?.totalGamesPlayed ?: 0,
                    totalTokensEarned = existing?.totalTokensEarned ?: 0,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    authProvider = PlayerProfileEntity.AUTH_PROVIDER_GOOGLE,
                    firebaseUid = user.uid,
                    email = user.email
                )
                saveProfile(profileEntity)
                if (currentUser != null && currentUser.isAnonymous) {
                    restoreUserData()
                }
            }
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This Google account is already linked to another account. Please sign in directly."
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid Google credentials. Please try again."
                    "ERROR_USER_DISABLED" -> "This Google account has been disabled."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Google sign-in failed"
                }
                else -> e.message ?: "Google sign-in failed"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult<Unit> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val existing = getProfileEntity()
                val newProfile = if (existing != null && existing.firebaseUid == null) {
                    PlayerProfileEntity(
                        id = existing.id,
                        nickname = existing.nickname,
                        username = existing.username,
                        avatarId = existing.avatarId,
                        totalGamesPlayed = existing.totalGamesPlayed,
                        totalTokensEarned = existing.totalTokensEarned,
                        createdAt = existing.createdAt,
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                        firebaseUid = user.uid,
                        email = email
                    )
                } else if (existing == null) {
                    PlayerProfileEntity(
                        id = "local_profile",
                        nickname = user.displayName ?: email.substringBefore("@"),
                        username = email.substringBefore("@"),
                        avatarId = "avatar_1",
                        totalGamesPlayed = 0,
                        totalTokensEarned = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                        firebaseUid = user.uid,
                        email = email
                    )
                } else {
                    existing
                }
                saveProfile(newProfile)
                restoreUserData()
            }
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Email sign-in failed"
                }
                else -> e.message ?: "Email sign-in failed"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val profile = PlayerProfileEntity(
                    id = "local_profile",
                    nickname = user.displayName ?: email.substringBefore("@"),
                    username = email.substringBefore("@"),
                    avatarId = "avatar_1",
                    totalGamesPlayed = 0,
                    totalTokensEarned = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    authProvider = PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                    firebaseUid = user.uid,
                    email = email
                )
                saveProfile(profile)
                sendEmailVerificationInternal()
            }
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email address."
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please use at least 6 characters."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Email sign-up failed"
                }
                else -> e.message ?: "Email sign-up failed"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun signInAsGuest(): AuthResult<Unit> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            result.user?.let { user ->
                val profile = PlayerProfileEntity(
                    id = "local_profile",
                    nickname = "Guest",
                    username = "",
                    avatarId = "avatar_1",
                    totalGamesPlayed = 0,
                    totalTokensEarned = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    authProvider = PlayerProfileEntity.AUTH_PROVIDER_GUEST,
                    firebaseUid = user.uid,
                    email = null
                )
                saveProfile(profile)
            }
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Guest sign-in failed")
        }
    }

    override suspend fun linkGuestToEmail(email: String, password: String): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return AuthResult.Error("No active session to link")
            }
            if (user.isAnonymous.not()) {
                return AuthResult.Error("Account is already linked")
            }
            backupUserData()
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            result.user?.let { fbUser ->
                val existing = getProfileEntity()
                val newProfile = if (existing != null) {
                    PlayerProfileEntity(
                        id = existing.id,
                        nickname = existing.nickname,
                        username = existing.username,
                        avatarId = existing.avatarId,
                        totalGamesPlayed = existing.totalGamesPlayed,
                        totalTokensEarned = existing.totalTokensEarned,
                        createdAt = existing.createdAt,
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                        firebaseUid = fbUser.uid,
                        email = email
                    )
                } else {
                    PlayerProfileEntity(
                        id = "local_profile",
                        nickname = email.substringBefore("@"),
                        username = email.substringBefore("@"),
                        avatarId = "avatar_1",
                        totalGamesPlayed = 0,
                        totalTokensEarned = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                        firebaseUid = fbUser.uid,
                        email = email
                    )
                }
                saveProfile(newProfile)
                restoreUserData()
            }
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email address."
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password."
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please use at least 6 characters."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Failed to link account"
                }
                else -> e.message ?: "Failed to link account"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun linkGuestToGoogle(idToken: String): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return AuthResult.Error("No active session to link")
            }
            if (user.isAnonymous.not()) {
                return AuthResult.Error("Account is already linked")
            }
            backupUserData()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = user.linkWithCredential(credential).await()
            result.user?.let { fbUser ->
                val existing = getProfileEntity()
                val newProfile = if (existing != null) {
                    PlayerProfileEntity(
                        id = existing.id,
                        nickname = existing.nickname,
                        username = existing.username,
                        avatarId = existing.avatarId,
                        totalGamesPlayed = existing.totalGamesPlayed,
                        totalTokensEarned = existing.totalTokensEarned,
                        createdAt = existing.createdAt,
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_GOOGLE,
                        firebaseUid = fbUser.uid,
                        email = fbUser.email
                    )
                } else {
                    PlayerProfileEntity(
                        id = "local_profile",
                        nickname = fbUser.displayName ?: "Player",
                        username = "",
                        avatarId = "avatar_1",
                        totalGamesPlayed = 0,
                        totalTokensEarned = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        authProvider = PlayerProfileEntity.AUTH_PROVIDER_GOOGLE,
                        firebaseUid = fbUser.uid,
                        email = fbUser.email
                    )
                }
                saveProfile(newProfile)
                restoreUserData()
            }
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This Google account is already linked to another account."
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid Google credentials. Please try again."
                    "ERROR_USER_DISABLED" -> "This Google account has been disabled."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Failed to link Google account"
                }
                else -> e.message ?: "Failed to link Google account"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-out failed")
        }
    }

    override suspend fun resetLocalData(): AuthResult<Unit> {
        return try {
            tokenRepository.reset()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to reset local data")
        }
    }

    override suspend fun sendEmailVerification(): AuthResult<Unit> {
        return try {
            sendEmailVerificationInternal()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send verification email")
        }
    }

    override suspend fun reloadUser(): AuthResult<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to reload user")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            val message = when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                    else -> e.message ?: "Failed to send password reset email"
                }
                else -> e.message ?: "Failed to send password reset email"
            }
            AuthResult.Error(message)
        }
    }

    override suspend fun backupUserData(): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.Error("Not signed in")
            val profile = getProfileEntity() ?: return AuthResult.Success
            val data = hashMapOf<String, Any>(
                "nickname" to profile.nickname,
                "username" to profile.username,
                "avatarId" to profile.avatarId,
                "totalGamesPlayed" to profile.totalGamesPlayed,
                "totalTokensEarned" to profile.totalTokensEarned,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt,
                "authProvider" to profile.authProvider,
                "email" to (profile.email ?: "")
            )
            firestore.collection("user_backups")
                .document(user.uid)
                .set(data)
                .await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Backup failed")
        }
    }

    override suspend fun restoreUserData(): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.Error("Not signed in")
            val snapshot = firestore.collection("user_backups")
                .document(user.uid)
                .get()
                .await()
            if (snapshot.exists()) {
                val existing = getProfileEntity()
                val restored = PlayerProfileEntity(
                    id = "local_profile",
                    nickname = snapshot.getString("nickname") ?: (existing?.nickname ?: "Player"),
                    username = snapshot.getString("username") ?: (existing?.username ?: ""),
                    avatarId = snapshot.getString("avatarId") ?: (existing?.avatarId ?: "avatar_1"),
                    totalGamesPlayed = snapshot.getLong("totalGamesPlayed")?.toInt() ?: (existing?.totalGamesPlayed ?: 0),
                    totalTokensEarned = snapshot.getLong("totalTokensEarned")?.toInt() ?: (existing?.totalTokensEarned ?: 0),
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    authProvider = snapshot.getString("authProvider") ?: PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                    firebaseUid = user.uid,
                    email = snapshot.getString("email")?.ifEmpty { null }
                )
                saveProfile(restored)
            }
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Restore failed")
        }
    }

    override suspend fun updateNickname(nickname: String): AuthResult<Unit> {
        return try {
            val existing = getProfileEntity() ?: return AuthResult.Error("No profile found")
            saveProfile(existing.copy(nickname = nickname, updatedAt = System.currentTimeMillis()))
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update nickname")
        }
    }

    override suspend fun updateUsername(username: String): AuthResult<Unit> {
        return try {
            val validation = com.LetterQuest.domain.model.UsernameValidator.validate(username)
            val validatedUsername = when (validation) {
                is com.LetterQuest.domain.model.ValidationResult.Success -> validation.value
                is com.LetterQuest.domain.model.ValidationResult.Error -> return AuthResult.Error(validation.message)
            }
            val existing = getProfileEntity() ?: return AuthResult.Error("No profile found")
            saveProfile(existing.copy(username = validatedUsername, updatedAt = System.currentTimeMillis()))
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update username")
        }
    }

    override suspend fun updateAvatar(avatarId: String): AuthResult<Unit> {
        return try {
            val existing = getProfileEntity() ?: return AuthResult.Error("No profile found")
            saveProfile(existing.copy(avatarId = avatarId, updatedAt = System.currentTimeMillis()))
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update avatar")
        }
    }

    override suspend fun importProfile(profile: PlayerProfile): AuthResult<Unit> {
        return try {
            val existing = getProfileEntity()
            val entity = PlayerProfileEntity(
                id = existing?.id ?: "local_profile",
                nickname = profile.nickname,
                username = profile.username,
                avatarId = profile.avatarId,
                totalGamesPlayed = profile.totalGamesPlayed,
                totalTokensEarned = profile.totalTokensEarned,
                createdAt = profile.createdAt,
                updatedAt = System.currentTimeMillis(),
                authProvider = existing?.authProvider ?: PlayerProfileEntity.AUTH_PROVIDER_EMAIL,
                firebaseUid = existing?.firebaseUid ?: profile.playerId,
                email = existing?.email ?: profile.username
            )
            saveProfile(entity)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to import profile")
        }
    }

    private suspend fun sendEmailVerificationInternal() {
        val user = firebaseAuth.currentUser ?: return
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setHandleCodeInApp(true)
            .build()
        user.sendEmailVerification(actionCodeSettings).await()
    }
}
