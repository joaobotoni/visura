package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import com.botoni.visura.domain.usecase.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class SignUpState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val confirmPassword: Password = Password(""),
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

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

    fun signUpWithEmail(){
         try {

         } catch (e: AuthenticationException) {

         }
    }

    fun signUpWithGoogle(){

    }
}

