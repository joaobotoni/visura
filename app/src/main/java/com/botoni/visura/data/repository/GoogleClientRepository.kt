package com.botoni.visura.data.repository

import com.botoni.visura.data.datasource.GoogleClientRemoteDataSource
import javax.inject.Inject

class GoogleClientRepository @Inject constructor(private val googleClientRemoteDataSource: GoogleClientRemoteDataSource) {
    suspend fun signInWithGoogle() =
        googleClientRemoteDataSource.signInWithGoogle()
    suspend fun signOut() =
        googleClientRemoteDataSource.signOut();

}