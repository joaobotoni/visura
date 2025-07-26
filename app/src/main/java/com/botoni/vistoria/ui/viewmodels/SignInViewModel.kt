package com.botoni.vistoria.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.vistoria.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLogged: Boolean = false,
    val passwordVisibility: Boolean = false,
    val Error: String? = null,
    val Success: String? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    init {
        checkUserLoggedIn()
    }

    fun checkUserLoggedIn() {
        _uiState.update { it.copy(isLogged = auth.currentUser != null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisibility = !it.passwordVisibility) }
    }

    fun signIn() {
        val (email, password) = _uiState.value.let { it.email to it.password }

        isCheckEmailAndPassword(email, password)

        viewModelScope.launch {
            runCatching {
                authenticationUseCase.signUp(email, password)
                _uiState.update { it.copy(isLogged = true, Success = "Success creating user") }
            }.recoverCatching { exception ->
                when (exception) {
                    is FirebaseAuthUserCollisionException -> {
                        authenticationUseCase.signIn(email, password)
                        _uiState.update { it.copy(isLogged = true, Success = "Authentication success") }
                    }
                    else -> throw exception
                }
            }.onFailure { exception ->
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Error email or password invalid"
                    else -> "Error trying to authenticate"
                }
                _uiState.update { it.copy(isLogged = false, Error = errorMessage) }
            }
        }
    }

    fun isCheckEmailAndPassword(email: String, password: String) {
        when {
            email.isBlank() -> _uiState.update {
                it.copy(
                    isLogged = false,
                    Error = "Email cannot be empty"
                )
            }

            password.isBlank() -> _uiState.update {
                it.copy(
                    isLogged = false,
                    Error = "Password cannot be empty"
                )
            }

            password.length <= 6 -> _uiState.update {
                it.copy(
                    isLogged = false,
                    Error = "Your password cannot be less than 6 characters long"
                )
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                authenticationUseCase.signInWithGoogle()
                _uiState.update { it.copy(isLogged = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLogged = false,
                        Error = "Error trying to authenticate with Google: $e"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(Error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(Success = null) }
    }
}