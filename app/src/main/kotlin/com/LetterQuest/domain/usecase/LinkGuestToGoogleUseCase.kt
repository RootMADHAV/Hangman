package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.repository.AuthRepository
import javax.inject.Inject

class LinkGuestToGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult<Unit> =
        authRepository.linkGuestToGoogle(idToken)
}
