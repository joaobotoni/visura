package com.botoni.visura.data.repository.authentication

import com.botoni.visura.data.datasource.authentication.GoogleClientRemoteDataSource
import javax.inject.Inject

class GoogleClientRepository @Inject constructor(private val googleClientRemoteDataSource: GoogleClientRemoteDataSource) {
    suspend fun signInWithGoogle() = googleClientRemoteDataSource.signInWithGoogle()
    suspend fun signUpWithGoogle() = googleClientRemoteDataSource.signUpWithGoogle()
    suspend fun signOut() = googleClientRemoteDataSource.signOut();
}