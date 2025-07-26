package com.botoni.vistoria.data.repository

import android.util.Log
import com.botoni.vistoria.data.datasource.FireBaseClientRemoteDataSource
import javax.inject.Inject

class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
    suspend fun signIn(email: String, password: String) {
        fireBaseClientRemoteDataSource.createUser(email, password)
    }
    fun signOut() {
        fireBaseClientRemoteDataSource.signOut()
    }
}