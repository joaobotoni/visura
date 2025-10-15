package com.botoni.visura.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.R
import com.botoni.visura.domain.exceptions.authentication.AuthError
import com.botoni.visura.domain.exceptions.authentication.AuthenticationException
import com.botoni.visura.domain.model.authentication.Email
import com.botoni.visura.domain.model.authentication.Password
import com.botoni.visura.domain.usecase.authentication.AuthenticationUseCase
import dagger.Reusable
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

@Reusable
sealed interface SignInEvent {
    data class Success(val message: String) : SignInEvent
    data class Error(val message: String, val error: AuthError?) : SignInEvent
}

private class SignInValidator {
    fun validate(state: SignInState): Result<Pair<Email, Password>> = runCatching {
        checkEmail(state.email) to checkPassword(state.password)
    }

    private fun checkEmail(email: Email): Email =
        Email.access(email.value).getOrThrow()

    private fun checkPassword(password: Password): Password =
        Password.access(password.value).getOrThrow()
}
@Reusable
private class SignInEventMapper(private val context: Context) {

    fun toSuccess(): SignInEvent.Success =
        SignInEvent.Success(context.getString(R.string.success_message_login))

    fun toError(exception: Throwable): SignInEvent.Error {
        val message = exception.message ?: context.getString(R.string.unknown_message)
        val error = (exception as? AuthenticationException)?.let { AuthError.AUTHENTICATION }
        return SignInEvent.Error(message, error)
    }
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val auth: AuthenticationUseCase
) : ViewModel() {

    private val validator = SignInValidator()
    private val mapper = SignInEventMapper(context)

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