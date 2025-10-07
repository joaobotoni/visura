package com.botoni.visura.data.datasource

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class GoogleAuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class SignInFailed(cause: Throwable) : GoogleAuthException("Falha ao fazer login com Google", cause)
    class SignUpFailed(cause: Throwable) : GoogleAuthException("Falha ao criar conta com Google", cause)
    class UserCancelled : GoogleAuthException("Login com Google cancelado pelo usuário")
    class NoAccountFound : GoogleAuthException("Nenhuma conta Google encontrada")
    class NetworkError(cause: Throwable) : GoogleAuthException("Erro de conexão. Verifique sua internet", cause)
    class InvalidCredential : GoogleAuthException("Credencial do Google inválida")
}

class GoogleClientRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val webClientId: String = "307083149527-tdgum4cpjvj21ovjc0vsogq1bo77q6m5.apps.googleusercontent.com"

    suspend fun signInWithGoogle() {
        try {
            val credential = requestCredential(filterByAuthorizedAccounts = true, autoSelectEnabled = true)
            authenticateWithFirebase(credential)
        } catch (e: NoCredentialException) {
            signUpWithGoogle()
        }
    }

    suspend fun signUpWithGoogle() {
        val credential = requestCredential(filterByAuthorizedAccounts = false, autoSelectEnabled = false)
        authenticateWithFirebase(credential)
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
            throw GoogleAuthException.UserCancelled()
        } catch (e: NoCredentialException) {
            throw e
        } catch (e: GetCredentialException) {
            throw GoogleAuthException.NetworkError(e)
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
        val isValid = credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        if (!isValid) {
            throw GoogleAuthException.InvalidCredential()
        }
    }

    private fun extractIdToken(credential: Credential): String {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom((credential as CustomCredential).data)
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