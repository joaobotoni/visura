package com.botoni.vistoria.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.vistoria.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Idle : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String, val type: ErrorType) : UiState()
}

enum class ErrorType {
    VALIDATION, AUTHENTICATION, UNKNOWN
}

sealed class SignInEvent {
    data class ShowMessage(
        val message: String,
        val isSuccess: Boolean,
        val errorType: ErrorType? = null
    ) : SignInEvent()
    object NavigateToHome : SignInEvent()
}

data class FieldState(
    val value: String = "",
    val error: String? = null
) {
    val isValid: Boolean get() = error == null
}

data class SignInState(
    val email: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val showPassword: Boolean = false,
    val uiState: UiState = UiState.Idle,
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    private val _events = MutableSharedFlow<SignInEvent>()
    val state = _state.asStateFlow()
    val events = _events.asSharedFlow()

    fun setEmail(email: String) {
        updateState {
            copy(
                email = FieldState(email.trim()),
                uiState = if (uiState is UiState.Error) UiState.Idle else uiState
            )
        }
    }

    fun setPassword(password: String) {
        updateState {
            copy(
                password = FieldState(password),
                uiState = if (uiState is UiState.Error) UiState.Idle else uiState
            )
        }
    }

    fun togglePasswordVisibility() {
        updateState { copy(showPassword = !showPassword) }
    }

    fun clearErrorsAndState() {
        updateState {
            copy(
                email = email.copy(error = null),
                password = password.copy(error = null),
                uiState = UiState.Idle,
                isEmailLoading = false,
                isGoogleLoading = false
            )
        }
    }

    fun loginWithEmail() {
        if (!validateForm()) return

        viewModelScope.launch {
            try {
                updateState { copy(isEmailLoading = true) }

                authUseCase.signIn(
                    email = state.value.email.value,
                    password = state.value.password.value
                )
                emitEmailSuccess("Login realizado com sucesso!")
            } catch (e: Exception) {
                emitEmailError(e.toUiError())
            } finally {
                updateState { copy(isEmailLoading = false) }
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            try {
                updateState { copy(isGoogleLoading = true) }
                authUseCase.signInWithGoogle()
                emitGoogleSuccess("Login com Google realizado com sucesso!")

            } catch (e: Exception) {
                emitGoogleError(e.toUiError())
            } finally {
                updateState { copy(isGoogleLoading = false) }
            }
        }
    }

    private fun validateForm(): Boolean {
        val emailError = validateEmail(state.value.email.value)
        val passwordError = validatePassword(state.value.password.value)

        val hasErrors = emailError != null || passwordError != null

        if (hasErrors) {
            updateState {
                copy(
                    email = email.copy(error = emailError),
                    password = password.copy(error = passwordError),
                    uiState = UiState.Error(
                        message = emailError ?: passwordError ?: "Erro de validação",
                        type = ErrorType.VALIDATION
                    )
                )
            }

            val message = emailError ?: passwordError ?: "Preencha os campos corretamente"
            emitEvent(
                SignInEvent.ShowMessage(
                    message = message,
                    isSuccess = false,
                    errorType = ErrorType.VALIDATION
                )
            )
        }

        return !hasErrors
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email é obrigatório"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Formato de email inválido"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Senha é obrigatória"
        password.length < 6 -> "Senha muito fraca. \nUse no mínimo 6 caracteres"
        else -> null
    }

    private suspend fun emitEmailSuccess(message: String) {
        updateState { copy(uiState = UiState.Success(message)) }
        emitEvent(SignInEvent.ShowMessage(message, isSuccess = true))
        delay(300)
        emitEvent(SignInEvent.NavigateToHome)
    }

    private suspend fun emitGoogleSuccess(message: String) {
        updateState { copy(uiState = UiState.Success(message)) }
        emitEvent(SignInEvent.ShowMessage(message, isSuccess = true))
        delay(300)
        emitEvent(SignInEvent.NavigateToHome)
    }

    private fun emitEmailError(error: UiState.Error) {
        val isAuthError = error.type == ErrorType.AUTHENTICATION

        updateState {
            copy(
                uiState = error,
                email = if (isAuthError) email.copy(error = "Credenciais inválidas") else email,
                password = if (isAuthError) password.copy(error = "Credenciais inválidas") else password
            )
        }

        emitEvent(
            SignInEvent.ShowMessage(
                message = error.message,
                isSuccess = false,
                errorType = error.type
            )
        )
    }

    private fun emitGoogleError(error: UiState.Error) {
        updateState { copy(uiState = error) }

        emitEvent(
            SignInEvent.ShowMessage(
                message = error.message,
                isSuccess = false,
                errorType = error.type
            )
        )
    }

    private fun Throwable.toUiError(): UiState.Error = when (this) {
        is FirebaseAuthInvalidCredentialsException -> UiState.Error(
            message = "Email ou senha incorretos",
            type = ErrorType.AUTHENTICATION
        )
        is FirebaseAuthInvalidUserException -> UiState.Error(
            message = "Usuário não encontrado",
            type = ErrorType.AUTHENTICATION
        )
        is FirebaseAuthUserCollisionException -> UiState.Error(
            message = "Este email já está cadastrado",
            type = ErrorType.AUTHENTICATION
        )
        else -> UiState.Error(
            message = "Erro inesperado. Tente novamente",
            type = ErrorType.UNKNOWN
        )
    }

    private fun emitEvent(event: SignInEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun updateState(transform: SignInState.() -> SignInState) {
        _state.update(transform)
    }
}