package com.botoni.visura.data.datasource

import android.util.Log
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
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

    suspend fun signUp(email: Email, password: Password) {
        auth.createUserWithEmailAndPassword(email.value, password.value).await()
        logSuccess("Success registering the user")
    }

    suspend fun signIn(email: Email, password: Password) {
        auth.signInWithEmailAndPassword(email.value, password.value).await()
        logSuccess("Success in authenticating the user")
    }

    fun signOut() {
        auth.signOut()
        logSuccess("User signed out")
    }

    private fun logSuccess(message: String) {
        Log.d(TAG, message)
    }
}