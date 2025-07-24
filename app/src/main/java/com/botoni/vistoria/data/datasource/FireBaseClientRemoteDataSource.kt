package com.botoni.vistoria.data.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import javax.inject.Inject

class FireBaseClientRemoteDataSource @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FireBaseClientRemoteDataSource", "User created successfully!")
                } else {
                    Log.e("FireBaseClientRemoteDataSource", "Error creating user: ${task.exception?.message}")
                }
            }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FireBaseClientRemoteDataSource", "Successful login!")
                } else {
                    Log.e("FireBaseClientRemoteDataSource", "Error during login: ${task.exception?.message}")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        Log.d("FireBaseClientRemoteDataSource", "User signed out.")
    }
}