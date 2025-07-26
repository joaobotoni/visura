package com.botoni.vistoria.data.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireBaseClientRemoteDataSource @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "FireBaseAuth"
    }

    suspend fun signIn(email: String, password: String) {
        try {
            createUser(email, password)
        } catch (_: FirebaseAuthUserCollisionException) {
            signInUser(email, password)
        } catch (e: Exception) {
            logError("Authentication failed", e)
        }
    }

    private suspend fun createUser(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            logSuccess("User created successfully")
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun signInUser(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            logSuccess("User signed in successfully")
        } catch (e: Exception) {
            logError("Sign in failed", e)
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
        Log.e(TAG, "$message: ${exception.message}")
    }
}