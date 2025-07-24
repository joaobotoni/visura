package com.botoni.vistoria.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.vistoria.data.repository.FireBaseClientRepository
import com.botoni.vistoria.data.repository.GoogleClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GatewayUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisibility: Boolean = false,
)

@HiltViewModel
class GatewayViewModel @Inject constructor(
   private val googleClientRepository: GoogleClientRepository,
   private val fireBaseClientRepository: FireBaseClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GatewayUiState())
    val uiState: StateFlow<GatewayUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisibility = !it.passwordVisibility) }
    }

    fun signInWithEmailAndPassword(){
        val email = _uiState.value.email
        val password = _uiState.value.password
        viewModelScope.launch {
            fireBaseClientRepository.signInWithEmailAndPassword(email, password)
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            googleClientRepository.signInWithGoogle(context)
        }
    }
}