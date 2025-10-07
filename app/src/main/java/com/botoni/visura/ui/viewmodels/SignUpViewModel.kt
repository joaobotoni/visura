package com.botoni.visura.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.data.datasource.GoogleAuthException
import com.botoni.visura.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignUpEvent {
    data class ShowMessage(
        val message: String,
        val isSuccess: Boolean,
        val errorType: ErrorType? = null
    ) : SignUpEvent()

    object NavigateToHome : SignUpEvent()
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

    fun clearErrorsAndState() {
        updateState {
            copy(
                email = email.copy(error = null),
                password = password.copy(error = null),
                confirmPassword = confirmPassword.copy(error = null),
                uiState = UiState.Idle,
                isEmailLoading = false,
                isGoogleLoading = false
            )
        }
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
            successMessage = "Conta criada com sucesso!",
            onError = ::handleEmailSignUpError
        )
    }

    fun signUpWithGoogle() {
        executeAuthOperation(
            setLoading = { isLoading -> copy(isGoogleLoading = isLoading) },
            operation = { authUseCase.signUpWithGoogle() },
            successMessage = "Cadastro com Google realizado com sucesso!",
            onError = ::handleGoogleSignUpError
        )
    }
    private fun validateInputs(): Boolean {
        val emailError = validateEmail()
        val passwordError = validatePassword()
        val confirmPasswordError = validateConfirmPassword()

        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            showValidationErrors(emailError, passwordError, confirmPasswordError)
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
            password.length < 8 -> "A senha deve ter no mínimo 8 caracteres"
            !password.any { it.isUpperCase() } -> "Senha precisa ter ao menos uma letra maiúscula."
            !password.any { it.isLowerCase() } -> "Senha precisa ter ao menos uma letra minúscula"
            !password.any { it.isDigit() } -> "Senha precisa ter ao menos um número"
            !password.any { "!@#$%^&*()_+-=[]{}|;:'\",.<>?".contains(it) } ->
                "Senha precisa ter ao menos um caractere especial: ! @ # $ % ^ & *"
            else -> null
        }
    }

    private fun validateConfirmPassword(): String? {
        val password = state.value.password.value
        val confirmPassword = state.value.confirmPassword.value
        return when {
            confirmPassword.isBlank() -> "Confirmação de senha é obrigatória"
            confirmPassword != password -> "As senhas não coincidem"
            else -> null
        }
    }

    private fun showValidationErrors(
        emailError: String?,
        passwordError: String?,
        confirmPasswordError: String?
    ) {
        updateState {
            copy(
                email = email.copy(error = emailError),
                password = password.copy(error = passwordError),
                confirmPassword = confirmPassword.copy(error = confirmPasswordError),
                uiState = UiState.Error(
                    message = emailError ?: passwordError ?: confirmPasswordError ?: "Erro de validação",
                    type = ErrorType.VALIDATION
                )
            )
        }
        emitEvent(
            SignUpEvent.ShowMessage(
                message = emailError ?: passwordError ?: confirmPasswordError
                ?: "Preencha os campos corretamente",
                isSuccess = false,
                errorType = ErrorType.VALIDATION
            )
        )
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
        emitEvent(SignUpEvent.ShowMessage(message, isSuccess = true))
        delay(300)
        emitEvent(SignUpEvent.NavigateToHome)
    }
    private fun handleEmailSignUpError(error: Throwable) {
        val uiError = mapToUiError(error)
        val isAuthError = uiError.type == ErrorType.AUTHENTICATION
        updateState {
            copy(
                uiState = uiError,
                email = when (error) {
                    is FirebaseAuthUserCollisionException ->
                        email.copy(error = "Este email já está cadastrado")
                    is FirebaseAuthInvalidCredentialsException ->
                        email.copy(error = "Email inválido")
                    else -> if (isAuthError) email.copy(error = "Email inválido") else email
                },
                password = when (error) {
                    is FirebaseAuthWeakPasswordException ->
                        password.copy(error = "Senha muito fraca")
                    else -> if (isAuthError && error !is FirebaseAuthUserCollisionException) {
                        password.copy(error = "Senha inválida")
                    } else {
                        password
                    }
                }
            )
        }
        showErrorMessage(uiError)
    }

    private fun handleGoogleSignUpError(error: Throwable) {
        val uiError = mapToUiError(error)
        updateState { copy(uiState = uiError) }
        showErrorMessage(uiError)
    }

    private fun showErrorMessage(error: UiState.Error) {
        emitEvent(
            SignUpEvent.ShowMessage(
                message = error.message,
                isSuccess = false,
                errorType = error.type
            )
        )
    }
    private fun mapToUiError(exception: Throwable): UiState.Error = when (exception) {
        is FirebaseAuthInvalidCredentialsException ->
            UiState.Error("Email inválido", ErrorType.AUTHENTICATION)

        is FirebaseAuthUserCollisionException ->
            UiState.Error("Este email já está cadastrado", ErrorType.AUTHENTICATION)

        is FirebaseAuthWeakPasswordException ->
            UiState.Error("Senha muito fraca. Use no mínimo 6 caracteres", ErrorType.AUTHENTICATION)

        is GoogleAuthException.UserCancelled ->
            UiState.Error("Cadastro cancelado", ErrorType.CANCELLED)

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
    private fun updateState(transform: SignUpState.() -> SignUpState) {
        _state.update(transform)
    }

    private fun emitEvent(event: SignUpEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}