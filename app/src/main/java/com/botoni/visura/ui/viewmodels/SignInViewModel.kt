package com.botoni.visura.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import com.botoni.visura.data.datasource.GoogleAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

sealed class SignInEvent {
    data class ShowMessage(
        val message: String,
        val isSuccess: Boolean,
        val errorType: ErrorType? = null
    ) : SignInEvent()
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
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SignInEvent>()
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

    fun loginWithEmail() {
        if (!validateInputs()) return
        executeAuthOperation(
            setLoading = { isLoading -> copy(isEmailLoading = isLoading) },
            operation = {
                authUseCase.signIn(
                    email = state.value.email.value,
                    password = state.value.password.value
                )
            },
            successMessage = "Login realizado com sucesso!",
            onError = ::handleEmailLoginError
        )
    }

    fun loginWithGoogle() {
        executeAuthOperation(
            setLoading = { isLoading -> copy(isGoogleLoading = isLoading) },
            operation = { authUseCase.signInWithGoogle() },
            successMessage = "Login com Google realizado com sucesso!",
            onError = ::handleGoogleLoginError
        )
    }

    private fun validateInputs(): Boolean {
        val emailError = when {
            state.value.email.value.isBlank() -> "Email é obrigatório"
            !Patterns.EMAIL_ADDRESS.matcher(state.value.email.value)
                .matches() -> "Formato de email inválido"

            else -> null
        }
        val passwordError =
            if (state.value.password.value.isBlank()) "Senha é obrigatória" else null

        if (emailError != null || passwordError != null) {
            updateState {
                copy(
                    email = email.copy(error = emailError),
                    password = password.copy(error = passwordError),
                    uiState = UiState.Error(
                        emailError ?: passwordError ?: "Erro de validação",
                        ErrorType.VALIDATION
                    )
                )
            }
            emitEvent(
                SignInEvent.ShowMessage(
                    emailError ?: passwordError ?: "Preencha os campos corretamente",
                    false,
                    ErrorType.VALIDATION
                )
            )
            return false
        }
        return true
    }

    private fun executeAuthOperation(
        setLoading: SignInState.(Boolean) -> SignInState,
        operation: suspend () -> Unit,
        successMessage: String,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                updateState { setLoading(true) }
                operation()
                updateState { copy(uiState = UiState.Success(successMessage)) }
                emitEvent(SignInEvent.ShowMessage(successMessage, true))
            } catch (exception: Exception) {
                onError(exception)
            } finally {
                updateState { setLoading(false) }
            }
        }
    }

    private fun handleEmailLoginError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState {
            copy(
                uiState = uiError,
                email = if (uiError.type == ErrorType.AUTHENTICATION) email.copy(error = "Credenciais inválidas") else email,
                password = if (uiError.type == ErrorType.AUTHENTICATION) password.copy(error = "Credenciais inválidas") else password
            )
        }
        emitEvent(SignInEvent.ShowMessage(uiError.message, false, uiError.type))
    }

    private fun handleGoogleLoginError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState { copy(uiState = uiError) }
        emitEvent(SignInEvent.ShowMessage(uiError.message, false, uiError.type))
    }

    private fun mapToUiError(exception: Throwable): UiState.Error = when (exception) {
        is FirebaseAuthInvalidCredentialsException -> UiState.Error(
            "Email ou senha incorretos",
            ErrorType.AUTHENTICATION
        )

        is FirebaseAuthInvalidUserException -> UiState.Error(
            "Usuário não encontrado",
            ErrorType.AUTHENTICATION
        )

        is FirebaseAuthUserCollisionException -> UiState.Error(
            "Este email já está cadastrado",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.UserCancelled -> UiState.Error(
            "Login cancelado",
            ErrorType.CANCELLED
        )

        is GoogleAuthException.NoAccountFound -> UiState.Error(
            "Nenhuma conta Google encontrada",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.NetworkError -> UiState.Error("Erro de conexão", ErrorType.NETWORK)
        is GoogleAuthException.InvalidCredential -> UiState.Error(
            "Credencial do Google inválida",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.SignInFailed -> UiState.Error(
            "Falha ao fazer login com Google",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.SignUpFailed -> UiState.Error(
            "Falha ao criar conta com Google",
            ErrorType.AUTHENTICATION
        )

        else -> UiState.Error("Erro inesperado", ErrorType.UNKNOWN)
    }

    private fun updateState(transform: SignInState.() -> SignInState) {
        _state.update(transform)
    }

    private fun emitEvent(event: SignInEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}