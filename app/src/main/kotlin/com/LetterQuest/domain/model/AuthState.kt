package com.LetterQuest.domain.model

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val isGuest: Boolean,
        val isEmailVerified: Boolean = false,
        val username: String = ""
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
