package com.LetterQuest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LetterQuest.domain.model.AuthResult
import com.LetterQuest.domain.model.AuthState
import com.LetterQuest.domain.repository.AuthRepository
import com.LetterQuest.domain.usecase.BackupUserDataUseCase
import com.LetterQuest.domain.usecase.LinkGuestToEmailUseCase
import com.LetterQuest.domain.usecase.LinkGuestToGoogleUseCase
import com.LetterQuest.domain.usecase.ReloadUserUseCase
import com.LetterQuest.domain.usecase.RestoreUserDataUseCase
import com.LetterQuest.domain.usecase.SendEmailVerificationUseCase
import com.LetterQuest.domain.usecase.SendPasswordResetUseCase
import com.LetterQuest.domain.usecase.SignInAsGuestUseCase
import com.LetterQuest.domain.usecase.SignInWithEmailUseCase
import com.LetterQuest.domain.usecase.SignInWithGoogleUseCase
import com.LetterQuest.domain.usecase.SignOutUseCase
import com.LetterQuest.domain.usecase.SignUpWithEmailUseCase
import com.LetterQuest.domain.usecase.UpdateAvatarUseCase
import com.LetterQuest.domain.usecase.UpdateNicknameUseCase
import com.LetterQuest.domain.usecase.UpdateUsernameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val authState: AuthState = AuthState.Unauthenticated,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailLinkMode: Boolean = false,
    val isGoogleLinkMode: Boolean = false,
    val isSignUpMode: Boolean = false,
    val isForgotPasswordMode: Boolean = false,
    val passwordResetSent: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = authState is AuthState.Authenticated

    val currentUser: AuthState.Authenticated?
        get() = authState as? AuthState.Authenticated
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInAsGuestUseCase: SignInAsGuestUseCase,
    private val linkGuestToEmailUseCase: LinkGuestToEmailUseCase,
    private val linkGuestToGoogleUseCase: LinkGuestToGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val reloadUserUseCase: ReloadUserUseCase,
    private val backupUserDataUseCase: BackupUserDataUseCase,
    private val restoreUserDataUseCase: RestoreUserDataUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { authState ->
                _uiState.value = _uiState.value.copy(authState = authState, errorMessage = null)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signInWithGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    restoreUserDataUseCase()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signInWithEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    restoreUserDataUseCase()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signUpWithEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    sendEmailVerificationUseCase()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun signInAsGuest() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signInAsGuestUseCase()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun linkGuestToEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = linkGuestToEmailUseCase(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmailLinkMode = false
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun linkGuestToGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = linkGuestToGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isGoogleLinkMode = false
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = signOutUseCase()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            sendEmailVerificationUseCase()
        }
    }

    fun reloadUser() {
        viewModelScope.launch {
            reloadUserUseCase()
        }
    }

    fun backupData() {
        viewModelScope.launch {
            backupUserDataUseCase()
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            restoreUserDataUseCase()
        }
    }

    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        viewModelScope.launch {
            when (val result = updateNicknameUseCase(nickname)) {
                is AuthResult.Success -> { }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        viewModelScope.launch {
            when (val result = updateUsernameUseCase(username)) {
                is AuthResult.Success -> { }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateAvatar(avatarId: String) {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        viewModelScope.launch {
            when (val result = updateAvatarUseCase(avatarId)) {
                is AuthResult.Success -> { }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                else -> {}
            }
        }
    }

    fun setEmailLinkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEmailLinkMode = enabled, errorMessage = null)
    }

    fun setGoogleLinkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isGoogleLinkMode = enabled, errorMessage = null)
    }

    fun setSignUpMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isSignUpMode = enabled, errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setForgotPasswordMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isForgotPasswordMode = enabled, passwordResetSent = false, errorMessage = null)
    }

    fun sendPasswordReset(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = sendPasswordResetUseCase(email)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, passwordResetSent = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                else -> {}
            }
        }
    }
}
