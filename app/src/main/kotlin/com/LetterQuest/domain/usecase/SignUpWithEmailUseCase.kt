package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<Unit> =
        authRepository.signUpWithEmail(email, password)
}
