package com.botoni.vistoria.domain

import com.botoni.vistoria.data.repository.FireBaseClientRepository
import com.botoni.vistoria.data.repository.GoogleClientRepository
import javax.inject.Inject

class AuthenticationUseCase @Inject constructor(
    val fireBaseClientRepository: FireBaseClientRepository,
    val googleClientRepository: GoogleClientRepository
) {
    suspend fun signIn(email: String, password: String) {
        fireBaseClientRepository.singIn(email, password)
    }
    suspend fun signInWithGoogle() {
        googleClientRepository.signInWithGoogle()
    }

    suspend fun signOut(){
        fireBaseClientRepository.signOut()
        googleClientRepository.signOut()
    }
}