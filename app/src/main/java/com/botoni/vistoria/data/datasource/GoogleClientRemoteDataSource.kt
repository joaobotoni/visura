package com.botoni.vistoria.data.datasource

import android.util.Log
import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.ClearCredentialStateRequest
import com.botoni.vistoria.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleClientRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val credential: CredentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val webClient: String = context.getString(R.string.web_client)
    suspend fun signInWithGoogle(context: Context) {
        try {
            val response = buildCredentialRequest(context)
            handleSignIn(response.credential)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("GoogleSignIn", "Error in login white googl")
        }
    }

    private suspend fun buildCredentialRequest(context: Context): GetCredentialResponse {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClient)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credential.getCredential(request = request, context = context)
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val credential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Successful login with Google")
                }
            }
        } else {
            Log.d("GoogleSignIn", "Error in login white google")
        }
    }

    suspend fun signOut() {
        credential.clearCredentialState(ClearCredentialStateRequest())
        firebaseAuth.signOut()
    }
}
