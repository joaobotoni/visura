package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import com.botoni.visura.domain.usecase.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
)
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()
    private val state = uiState.value
    fun setEmail(email: Email) {
        _uiState.update { current ->
            current.copy(email = email)
        }
    }

    fun setPassword(password: Password) {
        _uiState.update { current ->
            current.copy(password = password)
        }
    }

    fun setConfirmPassword(confirm: Password) {
        _uiState.update { current ->
            current.copy(confirm = confirm)
        }
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

    private fun assertPasswordsMatch(): Password =
        state.password.takeIf { it.compareTo(state.confirm) == 0 }
            ?: throw AuthenticationException("Senhas não coincidem", Error.AUTHENTICATION)

    fun signUpWithEmail() {
        viewModelScope.launch {
            val email: Email = state.email
            val password: Password = assertPasswordsMatch()
            try {
                authenticationUseCase.signUp(email, password)
            } catch (exception: AuthenticationException) {

            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            try {
                authenticationUseCase.signUpWithGoogle()
            } catch (exception: AuthenticationException) {

            }
        }
    }
}

