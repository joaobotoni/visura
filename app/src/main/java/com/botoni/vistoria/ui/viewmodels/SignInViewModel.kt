package com.botoni.vistoria.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.vistoria.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignInEvent {
    data class ShowMessage(val message: String, val isSuccess: Boolean) : SignInEvent()
}

data class SignInState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthenticationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    private val _events = MutableSharedFlow<SignInEvent>()
    val state: StateFlow<SignInState> = _state.asStateFlow()
    val events: SharedFlow<SignInEvent> = _events.asSharedFlow()
    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun setPassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        val current = _state.value
        _state.value = current.copy(showPassword = !current.showPassword)
    }

    fun loginWithEmail() {
        val currentState = _state.value

        val validationError = validateLoginData(currentState.email, currentState.password)
        if (validationError != null) {
            showMessage(validationError, isSuccess = false)
            return
        }

        _state.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                authUseCase.signIn(currentState.email, currentState.password)
                handleLoginSuccess()
            } catch (e: Exception) {
                handleLoginError(e)
            }
        }
    }

    fun loginWithGoogle() {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                authUseCase.signInWithGoogle()
                handleLoginSuccess()
            } catch (e: Exception) {
                handleLoginError(e, "Erro ao fazer login com Google")
            }
        }
    }

    private fun validateLoginData(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email é obrigatório"
            password.isBlank() -> "Senha é obrigatória"
            password.length < 6 -> "Senha deve ter pelo menos 6 caracteres"
            !isValidEmail(email) -> "Email inválido"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    private fun handleLoginSuccess() {
        _state.value = _state.value.copy(
            isLoggedIn = true,
            isLoading = false
        )
        showMessage("Login realizado com sucesso!", isSuccess = true)
    }

    private fun handleLoginError(exception: Exception, defaultMessage: String = "Erro ao fazer login") {
        _state.value = _state.value.copy(isLoading = false)

        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Email ou senha incorretos"
            else -> defaultMessage
        }

        showMessage(errorMessage, isSuccess = false)
    }

    private fun showMessage(message: String, isSuccess: Boolean) {
        viewModelScope.launch {
            _events.emit(SignInEvent.ShowMessage(message, isSuccess))
        }
    }
}