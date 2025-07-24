package com.botoni.vistoria.data.repository

import android.util.Log
import androidx.compose.runtime.Immutable
import com.botoni.vistoria.data.datasource.FireBaseClientRemoteDataSource
import javax.inject.Inject

class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {

    fun signInDefault(email: String, password: String) {
        try {
            fireBaseClientRemoteDataSource.signInWithEmailAndPassword(email, password)
        } catch (e: Exception) {
            Log.d("", "Error in default login $e")
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String){
        try {
            fireBaseClientRemoteDataSource.createUserWithEmailAndPassword(email, password)
        } catch (e: Exception) {
            Log.d("", "Error in default create login $e")
        }
    }
}