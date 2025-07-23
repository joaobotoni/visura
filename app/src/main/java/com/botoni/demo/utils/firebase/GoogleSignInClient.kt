package com.botoni.demo.utils.firebase

import com.botoni.demo.R
import android.widget.Toast
import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.ClearCredentialStateRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import android.util.Log

class GoogleSignInClient(private val context: Context) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val authentication = FirebaseAuth.getInstance()
    private val TAG = "GoogleSignInClient"

    private fun getWebClientId(): String {
        return context.getString(R.string.web_client_id)
    }

    suspend fun signInWithGoogle() {
        Log.d(TAG, "Attempting Google sign-in process.")
        try {
            val response = buildCredentialRequest()
            handleSignIn(response.credential)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed: ${e.localizedMessage}", e)
            Toast.makeText(context, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun buildCredentialRequest(): GetCredentialResponse {
        Log.d(TAG, "Building credential request for Google Sign-In.")
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getWebClientId())
            .setFilterByAuthorizedAccounts(true)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    private fun handleSignIn(credential: Credential) {
        Log.d(TAG, "Handling sign-in credential.")
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val credential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            Log.d(TAG, "Google ID token obtained. Attempting Firebase sign-in with credential.")
            authentication.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Successful in login with Google", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Firebase sign-in with Google successful for user: ${authentication.currentUser?.email}")
                    authentication.currentUser
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error during Firebase Google sign-in."
                    Toast.makeText(context, "Error in login with Google: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Firebase sign-in with Google failed: $errorMessage", task.exception)
                }
            }
        } else {
            val errorMessage = "Credential is not a Google ID token."
            Toast.makeText(context, "Error in login with Google: $errorMessage", Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMessage)
        }
    }

    suspend fun signOut() {
        Log.d(TAG, "Attempting Google sign-out.")
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        authentication.signOut()
        Log.d(TAG, "Google sign-out and Firebase sign-out complete.")
    }
}