package com.botoni.vistoria.data.repository

import android.util.Log
import com.botoni.vistoria.data.datasource.FireBaseClientRemoteDataSource
import javax.inject.Inject

class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
    suspend fun singIn(email: String, password: String) {
        try {
            fireBaseClientRemoteDataSource.signIn(email, password)
        } catch (e: Exception) {
            Log.d("FireBaseClientRepository", "Error in default create login $e")
        }
    }
    fun signOut() {
        fireBaseClientRemoteDataSource.signOut()
    }
}