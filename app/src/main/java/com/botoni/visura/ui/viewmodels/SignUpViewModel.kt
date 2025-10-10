package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import com.botoni.visura.domain.usecase.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val confirm: Password = Password(""),
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false
)

data class SignUpEvent(
    val message: String,
    val isSuccess: Boolean,
    val error: Error? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignUpEvent>()
    val event = _uiEvent.asSharedFlow()

    fun updateEmail(email: Email) {
        _uiState.update { current -> current.copy(email = email) }
    }

    fun updatePassword(password: Password) {
        _uiState.update { current -> current.copy(password = password) }
    }

    fun updateConfirmPassword(confirm: Password) {
        _uiState.update { current -> current.copy(confirm = confirm) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { current ->
            current.copy(showPassword = !current.showPassword)
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { current ->
            current.copy(showConfirmPassword = !current.showConfirmPassword)
        }
    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            startEmailLoading()
            executeEmailSignUp()
            stopEmailLoading()
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            startGoogleLoading()
            executeGoogleSignUp()
            stopGoogleLoading()
        }
    }

    private fun startEmailLoading() {
        _uiState.update { current -> current.copy(isEmailLoading = true) }
    }

    private fun stopEmailLoading() {
        _uiState.update { current -> current.copy(isEmailLoading = false) }
    }

    private fun startGoogleLoading() {
        _uiState.update { current -> current.copy(isGoogleLoading = true) }
    }

    private fun stopGoogleLoading() {
        _uiState.update { current -> current.copy(isGoogleLoading = false) }
    }

    private suspend fun executeEmailSignUp() {
        val result = performEmailSignUp()
        notifyEvent(result)
    }

    private suspend fun executeGoogleSignUp() {
        val result = performGoogleSignUp()
        notifyEvent(result)
    }

    private suspend fun performEmailSignUp(): SignUpEvent {
        return try {
            val email = _uiState.value.email
            val password = validatePasswordMatch()
            authenticationUseCase.signUp(email, password)
            delay(1500L)
            successEvent("Registro com email efetuado com sucesso")
        } catch (exception: AuthenticationException) {
            errorEvent(exception)
        }
    }

    private suspend fun performGoogleSignUp(): SignUpEvent {
        return try {
            authenticationUseCase.signUpWithGoogle()
            delay(1500L)
            successEvent("Registro com Google efetuado com sucesso")
        } catch (exception: AuthenticationException) {
            errorEvent(exception)
        }
    }

    private fun validatePasswordMatch(): Password {
        val currentState = _uiState.value
        return when (currentState.password == currentState.confirm) {
            true -> currentState.password
            false -> throw AuthenticationException(
                Error.VALIDATION,
                "Senhas não coincidem"
            )
        }
    }

    private suspend fun notifyEvent(signUpEvent: SignUpEvent) {
        _uiEvent.emit(signUpEvent)
    }

    private fun successEvent(message: String): SignUpEvent {
        return SignUpEvent(
            message = message,
            isSuccess = true,
            error = null
        )
    }

    private fun errorEvent(exception: AuthenticationException): SignUpEvent {
        return SignUpEvent(
            message = exception.message ?: "Erro desconhecido",
            isSuccess = false,
            error = Error.AUTHENTICATION
        )
    }
}