package com.botoni.visura.data.datasource.authentication

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.botoni.visura.domain.exceptions.authentication.AuthenticationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleClientRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.Companion.create(context)
    private val webClientId: String = "307083149527-tdgum4cpjvj21ovjc0vsogq1bo77q6m5.apps.googleusercontent.com"

    suspend fun signInWithGoogle() {
        try {
            val credential = requestCredential(filterByAuthorizedAccounts = true, autoSelectEnabled = true)
            authenticateWithFirebase(credential)
        } catch (e: NoCredentialException) {
            signUpWithGoogle()
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException.GoogleSignInFailed(e)
        }
    }

    suspend fun signUpWithGoogle() {
        try {
            val credential = requestCredential(filterByAuthorizedAccounts = false, autoSelectEnabled = false)
            authenticateWithFirebase(credential)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException.GoogleSignUpFailed(e)
        }
    }

    suspend fun signOut() {
        clearCredentialState()
        firebaseAuth.signOut()
    }

    private suspend fun requestCredential(
        filterByAuthorizedAccounts: Boolean,
        autoSelectEnabled: Boolean
    ): Credential {
        return try {
            val googleIdOption = buildGoogleIdOption(filterByAuthorizedAccounts, autoSelectEnabled)
            val request = buildCredentialRequest(googleIdOption)
            val response = getCredentialFromManager(request)
            response.credential
        } catch (e: GetCredentialCancellationException) {
            throw AuthenticationException.UserCancelled()
        } catch (e: NoCredentialException) {
            throw AuthenticationException.GoogleNoAccountFound()
        } catch (e: GetCredentialException) {
            throw AuthenticationException.NetworkError(cause = e)
        }
    }

    private fun buildGoogleIdOption(
        filterByAuthorizedAccounts: Boolean,
        autoSelectEnabled: Boolean
    ): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setAutoSelectEnabled(autoSelectEnabled)
            .build()
    }

    private fun buildCredentialRequest(googleIdOption: GetGoogleIdOption): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private suspend fun getCredentialFromManager(request: GetCredentialRequest): GetCredentialResponse {
        return credentialManager.getCredential(request = request, context = context)
    }

    private suspend fun authenticateWithFirebase(credential: Credential) {
        validateCredential(credential)
        val idToken = extractIdToken(credential)
        signInWithFirebase(idToken)
    }

    private fun validateCredential(credential: Credential) {
        val isValid =
            credential is CustomCredential && credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        if (!isValid) {
            throw AuthenticationException.GoogleInvalidCredential()
        }
    }

    private fun extractIdToken(credential: Credential): String {
        val googleIdTokenCredential =
            GoogleIdTokenCredential.Companion.createFrom((credential as CustomCredential).data)
        return googleIdTokenCredential.idToken
    }

    private suspend fun signInWithFirebase(idToken: String) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(authCredential).await()
    }

    private suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}