package com.botoni.demo.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.demo.utils.firebase.DefaultLoginClient
import com.botoni.demo.utils.firebase.GoogleSignInClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel(context: Context) : ViewModel() {

    private val defaultLoginClient = DefaultLoginClient(context)
    private val googleSignInClient = GoogleSignInClient(context)
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()
    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    private val _passwordVisibility = MutableStateFlow(false)
    val passwordVisibility = _passwordVisibility.asStateFlow()
    fun onEmailChange(email: String) {
        _email.value = email
    }
    fun onPasswordChange(password: String) {
        _password.value = password
    }
    fun togglePasswordVisibility() {
        _passwordVisibility.value = !_passwordVisibility.value
    }

    fun loginWithEmailPassword() {
        val emailValue = _email.value
        val passwordValue = _password.value
        defaultLoginClient.signInUser(emailValue, passwordValue)
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            try {
                googleSignInClient.signInWithGoogle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        defaultLoginClient.signOut()
    }
}
