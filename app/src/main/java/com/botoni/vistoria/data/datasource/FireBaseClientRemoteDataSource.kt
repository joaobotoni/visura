package com.botoni.vistoria.data.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireBaseClientRemoteDataSource @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "FireBaseAuth"
    }

    suspend fun createUser(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            logSuccess("Success registering the user")
        } catch (e: Exception) {
            logError("Error registering user", e)
        }
    }

    suspend fun signInUser(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            logSuccess("Success in authenticating the user")
        } catch (e: Exception) {
            logError("Error registering user", e)
        }
    }

    fun signOut() {
        auth.signOut()
        logSuccess("User signed out")
    }

    private fun logSuccess(message: String) {
        Log.d(TAG, message)
    }

    private fun logError(message: String, exception: Exception) {
        Log.e(TAG, "$message: ${exception.message}", exception)
    }
}