package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.exceptions.AuthenticationException
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


data class SignUpState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val confirmPassword: Password = Password(""),
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
)

sealed class SignUpEvent {
    data class ShowMessage(
        val message: String?,
        val isSuccess: Boolean,
        val errorType: ErrorType? = null
    ) : SignUpEvent()
}

enum class ErrorType {
    VALIDATION, AUTHENTICATION, NETWORK, CANCELLED, UNKNOWN
}


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<SignUpEvent>()
    val events = _events.asSharedFlow()

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

    fun setConfirmPassword(confirmPassword: Password) {
        _uiState.update { current ->
            current.copy(confirmPassword = confirmPassword)
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

    fun signUpWithEmail() {
        viewModelScope.launch {
            val email: Email = uiState.value.email
            val password: Password = requireMatchingPasswords()
            try {
                authenticationUseCase.signUp(email, password)
                _events.emit(
                    value = SignUpEvent.ShowMessage(
                        message = "Cadastro feito com sucesso",
                        isSuccess = true
                    )
                )
            } catch (e: AuthenticationException) {
                _events.emit(
                    SignUpEvent.ShowMessage(
                        message = e.message,
                        isSuccess = false,
                        errorType = ErrorType.VALIDATION
                    )
                )
            }
        }
    }

    private fun requireMatchingPasswords(): Password =
        uiState.value.password.takeIf { it.compareTo(uiState.value.confirmPassword) == 0 }
            ?: throw AuthenticationException("Senhas não coincidem")

    fun signUpWithGoogle() {

    }
}

