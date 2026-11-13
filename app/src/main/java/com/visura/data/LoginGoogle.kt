package com.visura.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.visura.BuildConfig

// A conta escolhida no seletor do Google: o ID token vai para a API; nome e e-mail preenchem o cadastro
// quando ainda não há conta no Visura.
data class ContaGoogle(val idToken: String, val email: String, val nome: String)

val loginComGoogleConfigurado: Boolean get() = BuildConfig.GOOGLE_CLIENT_ID.isNotBlank()

// Abre o seletor de contas do Google (Credential Manager) e devolve o ID token, ou null se o usuário cancelar.
// O context precisa ser a Activity, que mostra o seletor.
suspend fun pedirContaGoogle(context: Context): ContaGoogle? {
    val opcao = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val pedido = GetCredentialRequest.Builder().addCredentialOption(opcao).build()

    val credencial = try {
        CredentialManager.create(context).getCredential(context, pedido).credential
    } catch (_: GetCredentialCancellationException) {
        return null
    } catch (_: NoCredentialException) {
        throw ApiErro("Nenhuma conta Google neste aparelho. Adicione uma conta nas configurações do Android.")
    } catch (e: GetCredentialException) {
        throw ApiErro("Não foi possível entrar com o Google: ${e.message ?: e.type}")
    }

    if (credencial !is CustomCredential || credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        throw ApiErro("O Google devolveu uma credencial inesperada.")
    }
    val google = GoogleIdTokenCredential.createFrom(credencial.data)
    return ContaGoogle(google.idToken, google.id, google.displayName.orEmpty())
}
