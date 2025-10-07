package com.botoni.visura.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.AuthenticationUseCase
import com.botoni.visura.data.datasource.GoogleAuthException
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
    VALIDATION,
    AUTHENTICATION,
    NETWORK,
    CANCELLED,
    UNKNOWN
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
        val emailError = validateEmail()
        val passwordError = validatePassword()

        if (emailError != null || passwordError != null) {
            showValidationErrors(emailError, passwordError)
            return false
        }

        return true
    }

    private fun validateEmail(): String? {
        val email = state.value.email.value
        return when {
            email.isBlank() -> "Email é obrigatório"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Formato de email inválido"
            else -> null
        }
    }

    private fun validatePassword(): String? {
        val password = state.value.password.value
        return when {
            password.isBlank() -> "Senha é obrigatória"
            else -> null
        }
    }

    private fun showValidationErrors(emailError: String?, passwordError: String?) {
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

        emitEvent(
            SignInEvent.ShowMessage(
                message = emailError ?: passwordError ?: "Preencha os campos corretamente",
                isSuccess = false,
                errorType = ErrorType.VALIDATION
            )
        )
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
                handleSuccess(successMessage)
            } catch (exception: Exception) {
                onError(exception)
            } finally {
                updateState { setLoading(false) }
            }
        }
    }

    private suspend fun handleSuccess(message: String) {
        updateState { copy(uiState = UiState.Success(message)) }
        emitEvent(SignInEvent.ShowMessage(message, isSuccess = true))
        delay(300)
        emitEvent(SignInEvent.NavigateToHome)
    }
    private fun handleEmailLoginError(error: Throwable) {
        val uiError = mapToUiError(error)
        val isAuthError = uiError.type == ErrorType.AUTHENTICATION

        updateState {
            copy(
                uiState = uiError,
                email = if (isAuthError) email.copy(error = "Credenciais inválidas") else email,
                password = if (isAuthError) password.copy(error = "Credenciais inválidas") else password
            )
        }
        showErrorMessage(uiError)
    }

    private fun handleGoogleLoginError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState { copy(uiState = uiError) }
        showErrorMessage(uiError)
    }

    private fun showErrorMessage(error: UiState.Error) {
        emitEvent(
            SignInEvent.ShowMessage(
                message = error.message,
                isSuccess = false,
                errorType = error.type
            )
        )
    }

    private fun mapToUiError(exception: Throwable): UiState.Error = when (exception) {
        is FirebaseAuthInvalidCredentialsException ->
            UiState.Error("Email ou senha incorretos", ErrorType.AUTHENTICATION)

        is FirebaseAuthInvalidUserException ->
            UiState.Error("Usuário não encontrado", ErrorType.AUTHENTICATION)

        is FirebaseAuthUserCollisionException ->
            UiState.Error("Este email já está cadastrado", ErrorType.AUTHENTICATION)

        is GoogleAuthException.UserCancelled ->
            UiState.Error("Login cancelado", ErrorType.CANCELLED)

        is GoogleAuthException.NoAccountFound ->
            UiState.Error("Nenhuma conta Google encontrada", ErrorType.AUTHENTICATION)

        is GoogleAuthException.NetworkError ->
            UiState.Error("Erro de conexão. Verifique sua internet", ErrorType.NETWORK)

        is GoogleAuthException.InvalidCredential ->
            UiState.Error("Credencial do Google inválida", ErrorType.AUTHENTICATION)

        is GoogleAuthException.SignInFailed ->
            UiState.Error("Falha ao fazer login com Google", ErrorType.AUTHENTICATION)

        is GoogleAuthException.SignUpFailed ->
            UiState.Error("Falha ao criar conta com Google", ErrorType.AUTHENTICATION)

        else ->
            UiState.Error("Erro inesperado. Tente novamente", ErrorType.UNKNOWN)
    }


    private fun updateState(transform: SignInState.() -> SignInState) {
        _state.update(transform)
    }

    private fun emitEvent(event: SignInEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}