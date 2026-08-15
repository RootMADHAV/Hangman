package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(avatarId: String): AuthResult<Unit> =
        authRepository.updateAvatar(avatarId)
}
