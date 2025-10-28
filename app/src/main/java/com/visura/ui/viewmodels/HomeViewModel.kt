package com.visura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visura.domain.usecase.authentication.AuthenticationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val authenticationUseCase: AuthenticationUseCase
) : ViewModel() {
    fun exit() {
        viewModelScope.launch { authenticationUseCase.signOut() }
    }
}