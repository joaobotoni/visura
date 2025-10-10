package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import com.botoni.visura.domain.usecase.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val emailLoading: Boolean = false,
    val googleLoading: Boolean = false
)

data class SignInEvent(
    val message: String,
    val success: Boolean,
    val error: Error? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: AuthenticationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<SignInEvent>()
    val event = _event.asSharedFlow()

    fun setEmail(email: Email) {
        _state.update { it.copy(email = email) }
    }

    fun setPassword(password: Password) {
        _state.update { it.copy(password = password) }
    }

    fun togglePassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            setEmailLoading(true)
            emailSignIn()
            setEmailLoading(false)
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            setGoogleLoading(true)
            googleSignIn()
            setGoogleLoading(false)
        }
    }

    private fun setEmailLoading(loading: Boolean) {
        _state.update { it.copy(emailLoading = loading) }
    }

    private fun setGoogleLoading(loading: Boolean) {
        _state.update { it.copy(googleLoading = loading) }
    }

    private suspend fun emailSignIn() {
        val event = try {
            val current = _state.value
            auth.signIn(current.email, current.password)
            createSuccess("Login efetuado com sucesso")
        } catch (e: AuthenticationException) {
            createError(e)
        }
        emit(event)
    }

    private suspend fun googleSignIn() {
        val event = try {
            auth.signInWithGoogle()
            createSuccess("Login com Google efetuado com sucesso")
        } catch (e: AuthenticationException) {
            createError(e)
        }
        emit(event)
    }

    private suspend fun emit(event: SignInEvent) {
        _event.emit(event)
    }

    private fun createSuccess(message: String) = SignInEvent(
        message = message,
        success = true,
        error = null
    )

    private fun createError(exception: AuthenticationException) = SignInEvent(
        message = exception.message ?: "Erro desconhecido",
        success = false,
        error = Error.AUTHENTICATION
    )
}