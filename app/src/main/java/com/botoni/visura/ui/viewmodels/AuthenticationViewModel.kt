package com.botoni.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthenticationState {
    data class Authenticated(val user: FirebaseUser) : AuthenticationState
    data object Unauthenticated : AuthenticationState
}

sealed interface AuthenticationEvent {
    data object NavigateToMain : AuthenticationEvent
    data object NavigateToSignIn : AuthenticationEvent
}

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<AuthenticationState>(
        auth.currentUser?.let { AuthenticationState.Authenticated(it) }
            ?: AuthenticationState.Unauthenticated
    )
    val state: StateFlow<AuthenticationState> = _state.asStateFlow()
    private val _event = Channel<AuthenticationEvent>(Channel.BUFFERED)
    val event: Flow<AuthenticationEvent> = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            callbackFlow {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val currentState = firebaseAuth.currentUser?.let {
                        AuthenticationState.Authenticated(it)
                    } ?: AuthenticationState.Unauthenticated
                    trySend(currentState)
                }
                auth.addAuthStateListener(listener)
                awaitClose { auth.removeAuthStateListener(listener) }
            }.collect { newState ->
                if (newState::class != _state.value::class) {
                    _state.value = newState
                    emit(newState)
                }
            }
        }
    }

    private suspend fun emit(state: AuthenticationState) {
        val event = when (state) {
            is AuthenticationState.Authenticated -> AuthenticationEvent.NavigateToMain
            AuthenticationState.Unauthenticated -> AuthenticationEvent.NavigateToSignIn
        }
        _event.send(event)
    }
}