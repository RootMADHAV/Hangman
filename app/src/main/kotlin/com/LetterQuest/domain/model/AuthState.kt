package com.LetterQuest.domain.model

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val isGuest: Boolean,
        val isEmailVerified: Boolean = false
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
