package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String): AuthResult<Unit> =
        authRepository.updateUsername(username)
}
