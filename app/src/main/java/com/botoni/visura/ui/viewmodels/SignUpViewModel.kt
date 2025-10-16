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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpState(
    val email: Email = Email(""),
    val password: Password = Password(""),
    val confirm: Password = Password(""),
    val showPassword: Boolean = false,
    val showConfirm: Boolean = false,
    val emailLoading: Boolean = false,
    val googleLoading: Boolean = false
)

sealed interface SignUpEvent {
    data class Success(val message: String) : SignUpEvent
    data class Error(val message: String, val error: AuthError?) : SignUpEvent
}

class SignUpValidator @Inject constructor() {
    fun validate(state: SignUpState): Result<Pair<Email, Password>> = runCatching {
        checkEmail(state.email) to checkPassword(state.password)
            .also { checkConfirm(it, state.confirm) }
    }

    private fun checkEmail(email: Email): Email =
        Email.create(email.value).getOrThrow()

    private fun checkPassword(password: Password): Password =
        Password.create(password.value).getOrThrow()

    private fun checkConfirm(password: Password, confirm: Password) {
        Password.confirm(confirm.value, password.value).getOrThrow()
    }
}

class SignUpEventMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun toSuccess(): SignUpEvent.Success =
        SignUpEvent.Success(context.getString(R.string.success_message_auth))

    fun toError(exception: Throwable): SignUpEvent.Error {
        val message = exception.message ?: context.getString(R.string.unknown_error_message)
        val error = (exception as? AuthenticationException)?.let { exception.error }
        return SignUpEvent.Error(message, error)
    }
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: AuthenticationUseCase,
    private val validator: SignUpValidator,
    private val mapper: SignUpEventMapper
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<SignUpEvent>()
    val event = _event.asSharedFlow()

    fun setEmail(email: Email) {
        _state.update { it.copy(email = email) }
    }

    fun setPassword(password: Password) {
        _state.update { it.copy(password = password) }
    }

    fun setConfirm(confirm: Password) {
        _state.update { it.copy(confirm = confirm) }
    }

    fun togglePassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun toggleConfirm() {
        _state.update { it.copy(showConfirm = !it.showConfirm) }
    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(emailLoading = true) }
                send(emailSignUp())
            } finally {
                _state.update { it.copy(emailLoading = false) }
            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(googleLoading = true) }
                send(googleSignUp())
            } finally {
                _state.update { it.copy(googleLoading = false) }
            }
        }
    }

    private suspend fun emailSignUp(): SignUpEvent {
        return validator.validate(_state.value)
            .mapCatching { (email, password) ->
                auth.signUp(email, password)
            }
            .fold(
                onSuccess = { mapper.toSuccess() },
                onFailure = { mapper.toError(it) }
            )
    }

    private suspend fun googleSignUp(): SignUpEvent {
        return runCatching {
            auth.signUpWithGoogle()
        }
            .fold(
                onSuccess = { mapper.toSuccess() },
                onFailure = { mapper.toError(it) }
            )
    }

    private suspend fun send(event: SignUpEvent) {
        _event.emit(event)
    }
}