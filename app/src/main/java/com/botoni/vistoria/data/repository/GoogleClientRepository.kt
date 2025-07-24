package com.botoni.vistoria.data.repository

import android.util.Log
import android.content.Context
import com.botoni.vistoria.data.datasource.GoogleClientRemoteDataSource
import javax.inject.Inject

class GoogleClientRepository @Inject constructor(private val googleClientRemoteDataSource: GoogleClientRemoteDataSource) {
    suspend fun signInWithGoogle(context: Context) {
        try {
            googleClientRemoteDataSource.signInWithGoogle(context)
        } catch (e: Exception) {
            Log.d("GoogleClientRepository", "Error in signIn with google $e")
        }
    }
}