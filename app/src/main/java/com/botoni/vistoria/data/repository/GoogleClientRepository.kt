package com.botoni.vistoria.data.repository

import android.util.Log
import android.content.Context
import com.botoni.vistoria.data.datasource.GoogleClientRemoteDataSource
import javax.inject.Inject

class GoogleClientRepository @Inject constructor(private val googleClientRemoteDataSource: GoogleClientRemoteDataSource) {
    suspend fun signInWithGoogle() =
        googleClientRemoteDataSource.signInWithGoogle()
    suspend fun signOut() =
        googleClientRemoteDataSource.signOut();

}