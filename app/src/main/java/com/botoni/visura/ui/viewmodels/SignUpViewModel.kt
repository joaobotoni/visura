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
    val showConfirm: Boolean = false,
    val emailLoading: Boolean = false,
    val googleLoading: Boolean = false
)

data class SignUpEvent(
    val message: String,
    val success: Boolean,
    val error: Error? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: AuthenticationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<SignUpEvent>()
    val event = _event.asSharedFlow()

    fun setEmail(email: Email) {
        _state.update { it.copy(email = email) }
    }

    fun setPassword(password: Password) {
        _state.update { it.copy(password = password) }
    }

    fun setConfirm(confirm: Password) {
        _state.update { it.copy(confirm = confirm) }
    }

    fun togglePassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun toggleConfirm() {
        _state.update { it.copy(showConfirm = !it.showConfirm) }
    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            setEmailLoading(true)
            performEmailSignUp()
            setEmailLoading(false)
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            setGoogleLoading(true)
            performGoogleSignUp()
            setGoogleLoading(false)
        }
    }

    private fun setEmailLoading(loading: Boolean) {
        _state.update { it.copy(emailLoading = loading) }
    }

    private fun setGoogleLoading(loading: Boolean) {
        _state.update { it.copy(googleLoading = loading) }
    }

    private suspend fun performEmailSignUp() {
        val event = try {
            val (email, password) = validate()
            auth.signUp(email, password)
            delay(1500L)
            createSuccess("Registro com email efetuado com sucesso")
        } catch (e: Exception) {
            createError(e)
        }
        emit(event)
    }

    private suspend fun performGoogleSignUp() {
        val event = try {
            auth.signUpWithGoogle()
            delay(1500L)
            createSuccess("Registro com Google efetuado com sucesso")
        } catch (e: AuthenticationException) {
            createError(e)
        }
        emit(event)
    }

    private fun validate(): Pair<Email, Password> {
        val email = validateEmail()
        val password = validatePassword()
        validateConfirm(password)
        return email to password
    }

    private fun validateEmail(): Email {
        return Email.create(_state.value.email.value).getOrThrow()
    }

    private fun validatePassword(): Password {
        return Password.create(_state.value.password.value).getOrThrow()
    }

    private fun validateConfirm(password: Password) {
        val confirm = Password.create(_state.value.confirm.value).getOrThrow()
        password.matches(confirm).getOrThrow()
    }

    private suspend fun emit(event: SignUpEvent) {
        _event.emit(event)
    }

    private fun createSuccess(message: String) = SignUpEvent(
        message = message,
        success = true,
        error = null
    )

    private fun createError(exception: Throwable) = SignUpEvent(
        message = exception.message ?: "Erro desconhecido",
        success = false,
        error = (exception as? AuthenticationException)?.let { Error.AUTHENTICATION }
    )
}