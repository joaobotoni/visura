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

data class SignInState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val showPassword: Boolean = false,
    val isEmailLoading: Boolean = false,
    val isGoogleLoading: Boolean = false
)

data class SignInEvent(
    val message: String,
    val isSuccess: Boolean,
    val error: Error? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInState())
    val uiState: StateFlow<SignInState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignInEvent>()
    val event = _uiEvent.asSharedFlow()

    fun updateEmail(email: Email) {
        _uiState.update { current -> current.copy(email = email) }
    }

    fun updatePassword(password: Password) {
        _uiState.update { current -> current.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { current ->
            current.copy(showPassword = !current.showPassword)
        }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            startEmailLoading()
            executeEmailSignIn()
            stopEmailLoading()
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            startGoogleLoading()
            executeGoogleSignIn()
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

    private suspend fun executeEmailSignIn() {
        val result = performEmailSignIn()
        notifyEvent(result)
    }

    private suspend fun executeGoogleSignIn() {
        val result = performGoogleSignIn()
        notifyEvent(result)
    }

    private suspend fun performEmailSignIn(): SignInEvent {
        return try {
            val currentState = _uiState.value
            authenticationUseCase.signIn(currentState.email, currentState.password)
            delay(1500L)
            successEvent("Login com email efetuado com sucesso")
        } catch (exception: AuthenticationException) {
            errorEvent(exception)
        }
    }

    private suspend fun performGoogleSignIn(): SignInEvent {
        return try {
            authenticationUseCase.signInWithGoogle()
            delay(1500L)
            successEvent("Login com Google efetuado com sucesso")
        } catch (exception: AuthenticationException) {
            errorEvent(exception)
        }
    }

    private suspend fun notifyEvent(signInEvent: SignInEvent) {
        _uiEvent.emit(signInEvent)
    }

    private fun successEvent(message: String): SignInEvent {
        return SignInEvent(
            message = message,
            isSuccess = true,
            error = null
        )
    }

    private fun errorEvent(exception: AuthenticationException): SignInEvent {
        return SignInEvent(
            message = exception.message ?: "Erro desconhecido",
            isSuccess = false,
            error = Error.AUTHENTICATION
        )
    }
}