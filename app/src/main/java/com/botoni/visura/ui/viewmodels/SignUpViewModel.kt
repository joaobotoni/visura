package com.botoni.visura.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.data.datasource.GoogleAuthException
import com.botoni.visura.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
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
    VALIDATION, AUTHENTICATION, NETWORK, CANCELLED, UNKNOWN
}

sealed class SignUpEvent {
    data class ShowMessage(
        val message: String,
        val isSuccess: Boolean,
        val errorType: ErrorType? = null
    ) : SignUpEvent()
}

data class FieldState(val value: String = "", val error: String? = null) {
    val isValid: Boolean get() = error == null
}

data class SignUpState(
    val email: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val confirmPassword: FieldState = FieldState(),
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val uiState: UiState = UiState.Idle,
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authUseCase: AuthenticationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<SignUpEvent>()
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

    fun setConfirmPassword(confirmPassword: String) {
        updateState {
            copy(
                confirmPassword = FieldState(confirmPassword),
                uiState = if (uiState is UiState.Error) UiState.Idle else uiState
            )
        }
    }

    fun togglePasswordVisibility() {
        updateState { copy(showPassword = !showPassword) }
    }

    fun toggleConfirmPasswordVisibility() {
        updateState { copy(showConfirmPassword = !showConfirmPassword) }
    }

    fun signUpWithEmail() {
        if (!validateInputs()) return
        executeAuthOperation(
            setLoading = { isLoading -> copy(isEmailLoading = isLoading) },
            operation = {
                authUseCase.signUp(
                    email = state.value.email.value,
                    password = state.value.password.value
                )
            },
            successMessage = "Conta criada!",
            onError = ::handleEmailSignUpError
        )
    }

    fun signUpWithGoogle() {
        executeAuthOperation(
            setLoading = { isLoading -> copy(isGoogleLoading = isLoading) },
            operation = { authUseCase.signUpWithGoogle() },
            successMessage = "Cadastro com Google concluído!",
            onError = ::handleGoogleSignUpError
        )
    }

    private fun validateInputs(): Boolean {
        val emailError = when {
            state.value.email.value.isBlank() -> "Email é obrigatório"
            !Patterns.EMAIL_ADDRESS.matcher(state.value.email.value).matches() -> "Email inválido"
            else -> null
        }
        val passwordError = when {
            state.value.password.value.isBlank() -> "Senha é obrigatória"
            state.value.password.value.length < 8 -> "Mínimo 8 caracteres"
            else -> null
        }
        val confirmPasswordError = when {
            state.value.confirmPassword.value.isBlank() -> "Confirme a senha"
            state.value.confirmPassword.value != state.value.password.value -> "Senhas não coincidem"
            else -> null
        }

        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            updateState {
                copy(
                    email = email.copy(error = emailError),
                    password = password.copy(error = passwordError),
                    confirmPassword = confirmPassword.copy(error = confirmPasswordError),
                    uiState = UiState.Error(
                        emailError ?: passwordError ?: confirmPasswordError ?: "Erro de validação",
                        ErrorType.VALIDATION
                    )
                )
            }
            emitEvent(
                SignUpEvent.ShowMessage(
                    emailError ?: passwordError ?: confirmPasswordError ?: "Preencha corretamente",
                    false,
                    ErrorType.VALIDATION
                )
            )
            return false
        }
        return true
    }

    private fun executeAuthOperation(
        setLoading: SignUpState.(Boolean) -> SignUpState,
        operation: suspend () -> Unit,
        successMessage: String,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                updateState { setLoading(true) }
                operation()
                updateState { copy(uiState = UiState.Success(successMessage)) }
                emitEvent(SignUpEvent.ShowMessage(successMessage, true))
            } catch (exception: Exception) {
                onError(exception)
            } finally {
                updateState { setLoading(false) }
            }
        }
    }

    private fun handleEmailSignUpError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState {
            copy(
                uiState = uiError,
                email = when (error) {
                    is FirebaseAuthUserCollisionException -> email.copy(error = "Email já cadastrado")
                    is FirebaseAuthInvalidCredentialsException -> email.copy(error = "Email inválido")
                    else -> email
                },
                password = when (error) {
                    is FirebaseAuthWeakPasswordException -> password.copy(error = "Senha fraca")
                    else -> password
                }
            )
        }
        emitEvent(SignUpEvent.ShowMessage(uiError.message, false, uiError.type))
    }

    private fun handleGoogleSignUpError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState { copy(uiState = uiError) }
        emitEvent(SignUpEvent.ShowMessage(uiError.message, false, uiError.type))
    }

    private fun mapToUiError(exception: Throwable): UiState.Error = when (exception) {
        is FirebaseAuthInvalidCredentialsException -> UiState.Error(
            "Email inválido",
            ErrorType.AUTHENTICATION
        )

        is FirebaseAuthUserCollisionException -> UiState.Error(
            "Email já cadastrado",
            ErrorType.AUTHENTICATION
        )

        is FirebaseAuthWeakPasswordException -> UiState.Error(
            "Senha fraca",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.UserCancelled -> UiState.Error(
            "Cadastro cancelado",
            ErrorType.CANCELLED
        )

        is GoogleAuthException.NoAccountFound -> UiState.Error(
            "Nenhuma conta Google",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.NetworkError -> UiState.Error("Erro de conexão", ErrorType.NETWORK)
        is GoogleAuthException.InvalidCredential -> UiState.Error(
            "Credencial inválida",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.SignInFailed -> UiState.Error(
            "Falha no login com Google",
            ErrorType.AUTHENTICATION
        )

        is GoogleAuthException.SignUpFailed -> UiState.Error(
            "Falha no cadastro com Google",
            ErrorType.AUTHENTICATION
        )

        else -> UiState.Error("Erro inesperado", ErrorType.UNKNOWN)
    }

    private fun updateState(transform: SignUpState.() -> SignUpState) {
        _state.update(transform)
    }

    private fun emitEvent(event: SignUpEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}