package com.botoni.demo.utils.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DefaultLoginClient(private val context: Context) {
    private var auth: FirebaseAuth = Firebase.auth
    private val TAG = "DefaultLoginClient"

    fun signInUser(email: String, password: String) {
        Log.d(TAG, "Attempting to sign in user: $email")

        when {
            email.isBlank() || password.isBlank() -> {
                val message = "E-mail e senha não podem estar vazios para login."
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Login failed: $message")
            }
            else -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Login successful for user: ${auth.currentUser?.email}")
                        } else {
                            val errorMessage = task.exception?.message ?: "Erro desconhecido ao fazer login."
                            Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Login failed for user $email: $errorMessage", task.exception)
                        }
                    }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        Toast.makeText(context, "Desconectado com sucesso.", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "User signed out successfully.")
    }

    fun getCurrentUser() = auth.currentUser
}