package com.botoni.vistoria.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.vistoria.domain.AuthenticationUseCase
import com.google.firebase.auth.FirebaseAuth
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
    val messageError: String? = null
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
        val email = _uiState.value.email
        val password = _uiState.value.password
        viewModelScope.launch {
            try {
                authenticationUseCase.signIn(email, password)
                _uiState.update { it.copy(isLogged = true) }
            }catch (e: Exception){
                _uiState.update { it.copy(isLogged = false, messageError = "Error trying to authenticate: $e") }
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
                        messageError = "Error trying to authenticate with Google: $e"
                    )
                }
            }
        }
    }
}