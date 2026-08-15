package com.LetterQuest.domain.model

sealed class AuthResult<out T> {
    data object Success : AuthResult<Unit>()
    data class Error(val message: String) : AuthResult<Nothing>()
    data class Data<T>(val data: T) : AuthResult<T>()
}
