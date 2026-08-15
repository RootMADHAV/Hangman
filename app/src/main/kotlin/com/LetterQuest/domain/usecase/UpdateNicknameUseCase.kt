package com.LetterQuest.domain.usecase

import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateNicknameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(nickname: String): AuthResult<Unit> =
        authRepository.updateNickname(nickname)
}
