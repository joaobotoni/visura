package com.botoni.visura.domain.usecase

import android.util.Log
import com.botoni.visura.data.repository.FireBaseClientRepository
import com.botoni.visura.data.repository.GoogleClientRepository
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import javax.inject.Inject

class AuthenticationUseCase @Inject constructor(
    val fireBaseClientRepository: FireBaseClientRepository,
    val googleClientRepository: GoogleClientRepository
) {
    suspend fun signIn(email: Email, password: Password) =
        fireBaseClientRepository.signIn(email, password)

    suspend fun signUp(email: Email, password: Password) =
        fireBaseClientRepository.signUp(email, password)

    suspend fun signInWithGoogle() =
        googleClientRepository.signInWithGoogle()

    suspend fun signUpWithGoogle() =
        googleClientRepository.signUpWithGoogle()

    suspend fun signOut() {
        fireBaseClientRepository.signOut()
        googleClientRepository.signOut()
    }
}