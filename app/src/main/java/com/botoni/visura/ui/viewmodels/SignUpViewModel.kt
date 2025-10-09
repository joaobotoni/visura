package com.botoni.visura.ui.viewmodels

import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import kotlinx.coroutines.flow.MutableStateFlow
import com.botoni.visura.domain.exceptions.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import com.botoni.visura.domain.usecase.AuthenticationUseCase
import com.botoni.visura.domain.exceptions.AuthenticationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class SignUpState(
    val email: Email? = null,
    val password: Password? = null,
    val confirm: Password? = null,
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

    fun setEmail(email: Email?) {
        _uiState.update { current ->
            current.copy(
                email = email
            )
        }
    }

    fun setPassword(password: Password?) {
        _uiState.update { current ->
            current.copy(
                password = password
            )
        }
    }

    fun setConfirmPassword(confirm: Password) {
        _uiState.update { current ->
            current.copy(
                confirm = confirm
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { current ->
            current.copy(
                showPassword = !current.showPassword
            )
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { current ->
            current.copy(
                showConfirmPassword = !current.showConfirmPassword
            )
        }
    }
    private fun assertEmailNotNull(): Email =
        _uiState.value.email
            ?: throw AuthenticationException(Error.VALIDATION, "E-mail não informado")

    private fun assertPasswordNotNull(): Password =
        _uiState.value.password
            ?: throw AuthenticationException(Error.VALIDATION, "Senha não informada")

    private fun assertConfirmPasswordNotNull(): Password =
        _uiState.value.confirm
            ?: throw AuthenticationException(Error.VALIDATION, "Confirmação de senha não informada")

    private fun assertPasswordsMatch(): Password =
        assertPasswordNotNull().takeIf { it == assertConfirmPasswordNotNull() }
            ?: throw AuthenticationException(Error.VALIDATION, "Senhas não coincidem")


    fun signUpWithEmail() {
        viewModelScope.launch {
            try {
                val email = assertEmailNotNull()
                val password = assertPasswordsMatch()
                authenticationUseCase.signUp(email, password)
                _uiEvent.emit(
                    SignUpEvent(
                        message = "Registro com Google efetuado com sucesso",
                        isSuccess = true,
                        error = null
                    )
                )
            } catch (exception: AuthenticationException) {
                _uiEvent.emit(
                    SignUpEvent(
                        message = exception.message ?: "Erro desconhecido",
                        isSuccess = false,
                        error = Error.AUTHENTICATION
                    )
                )
            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            try {
                authenticationUseCase.signUpWithGoogle()
                _uiEvent.emit(
                    SignUpEvent(
                        message = "Registro com Google efetuado com sucesso",
                        isSuccess = true,
                        error = null
                    )
                )
            } catch (exception: AuthenticationException) {
                _uiEvent.emit(
                    SignUpEvent(
                        message = exception.message ?: "Erro desconhecido",
                        isSuccess = false,
                        error = Error.AUTHENTICATION
                    )
                )
            }
        }
    }
}