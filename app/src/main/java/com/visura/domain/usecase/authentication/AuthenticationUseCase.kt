package com.visura.domain.usecase.authentication

import com.visura.data.repository.authentication.FireBaseClientRepository
import com.visura.data.repository.authentication.GoogleClientRepository
import com.visura.domain.model.authentication.Email
import com.visura.domain.model.authentication.Password
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