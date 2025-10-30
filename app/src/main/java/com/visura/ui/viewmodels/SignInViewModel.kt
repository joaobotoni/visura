package com.visura.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visura.R
import com.visura.domain.exceptions.authentication.AuthError
import com.visura.domain.exceptions.authentication.AuthenticationException
import com.visura.domain.model.authentication.Email
import com.visura.domain.model.authentication.Password
import com.visura.domain.usecase.authentication.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val showPassword: Boolean = false,
    val emailLoading: Boolean = false,
    val googleLoading: Boolean = false
)

sealed interface SignInEvent {
    data class Success(val message: String) : SignInEvent
    data class Error(val message: String, val error: AuthError?) : SignInEvent
}

class SignInValidator @Inject constructor() {
    fun validate(state: SignInState): Result<Pair<Email, Password>> = runCatching {
        checkEmail(state.email) to checkPassword(state.password)
    }

    private fun checkEmail(email: Email): Email =
            Email.access(email.value).getOrThrow()

    private fun checkPassword(password: Password): Password =
        Password.access(password.value).getOrThrow()
}

class SignInEventMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun toSuccess(): SignInEvent.Success =
        SignInEvent.Success(context.getString(R.string.success_message_auth))

    fun toError(exception: Throwable): SignInEvent.Error {
        val message = exception.message ?: context.getString(R.string.unknown_error_message)
        val error = (exception as? AuthenticationException)?.let { AuthError.AUTHENTICATION }
        return SignInEvent.Error(message, error)
    }
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: AuthenticationUseCase,
    private val validator: SignInValidator,
    private val mapper: SignInEventMapper
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<SignInEvent>()
    val event = _event.asSharedFlow()

    fun setEmail(email: Email) {
        _state.update { it.copy(email = email) }
    }

    fun setPassword(password: Password) {
        _state.update { it.copy(password = password) }
    }

    fun togglePassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(emailLoading = true) }
                send(emailSignIn())
            } finally {
                _state.update { it.copy(emailLoading = false) }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(googleLoading = true) }
                send(googleSignIn())
            } finally {
                _state.update { it.copy(googleLoading = false) }
            }
        }
    }

    private suspend fun emailSignIn(): SignInEvent {
        return validator.validate(_state.value)
            .mapCatching { (email, password) ->
                auth.signIn(email, password)
            }
            .fold(
                onSuccess = { mapper.toSuccess() },
                onFailure = { mapper.toError(it) }
            )
    }

    private suspend fun googleSignIn(): SignInEvent {
        return runCatching {
            auth.signInWithGoogle()
        }
            .fold(
                onSuccess = { mapper.toSuccess() },
                onFailure = { mapper.toError(it) }
            )
    }

    private suspend fun send(event: SignInEvent) {
        _event.emit(event)
    }
}