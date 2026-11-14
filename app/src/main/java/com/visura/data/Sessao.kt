package com.visura.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UsuarioLogado(val nome: String, val email: String)

// Tokens e dados do usuário logado, guardados no SharedPreferences privado do app.
// Tokens, nome, e-mail e CPF são gravados cifrados (AES-GCM com chave do Android Keystore, ver Cifra.kt).
// Valores antigos, em texto puro, são lidos uma vez e regravados cifrados. Se a chave se perder
// (dados restaurados em outro aparelho, por exemplo), o valor é descartado e o usuário entra de novo.
// O identificador do dispositivo é gerado uma vez e reaproveitado em todo login (a API abre uma sessão por dispositivo);
// não é segredo, então fica em texto puro.
class Sessao(
    context: Context,
    private val cifra: Cifra = CifraKeystore(),
    arquivo: String = ARQUIVO
) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(arquivo, Context.MODE_PRIVATE)

    private val _expirou = MutableStateFlow(false)

    // Vira true quando o refresh token é recusado; o NavGraph manda o usuário de volta ao login.
    val expirou: StateFlow<Boolean> = _expirou.asStateFlow()

    val accessToken: String? get() = ler(ACCESS)
    val refreshToken: String? get() = ler(REFRESH)
    val logado: Boolean get() = refreshToken != null

    val usuario: UsuarioLogado?
        get() {
            val nome = ler(NOME) ?: return null
            return UsuarioLogado(nome, ler(EMAIL).orEmpty())
        }

    // CPF digitado no cadastro. O perfil usa o CPF que vem de GET /users/me; este só cobre uma API antiga, sem o campo.
    var cpf: String?
        get() = ler(CPF)
        set(valor) = prefs.edit { putString(CPF, valor?.let(::cifrado)) }

    val dispositivo: String
        get() = prefs.getString(DISPOSITIVO, null) ?: UUID.randomUUID().toString().also { novo ->
            prefs.edit { putString(DISPOSITIVO, novo) }
        }

    fun salvarTokens(access: String, refresh: String) {
        prefs.edit {
            putString(ACCESS, cifrado(access))
            putString(REFRESH, cifrado(refresh))
        }
        _expirou.value = false
    }

    fun salvarUsuario(nome: String, email: String) {
        prefs.edit {
            putString(NOME, cifrado(nome))
            putString(EMAIL, cifrado(email))
        }
    }

    fun limpar(expirou: Boolean = false) {
        prefs.edit {
            remove(ACCESS)
            remove(REFRESH)
            remove(NOME)
            remove(EMAIL)
            remove(CPF)
        }
        _expirou.value = expirou
    }

    private fun cifrado(valor: String): String = PREFIXO + cifra.cifrar(valor)

    // Lê e decifra; migra o valor antigo em texto puro e descarta o que não dá para decifrar.
    @Synchronized
    private fun ler(chave: String): String? {
        val gravado = prefs.getString(chave, null) ?: return null
        if (!gravado.startsWith(PREFIXO)) {
            prefs.edit { putString(chave, cifrado(gravado)) }
            return gravado
        }
        return try {
            cifra.decifrar(gravado.removePrefix(PREFIXO))
        } catch (e: Exception) {
            prefs.edit { remove(chave) }
            null
        }
    }

    private companion object {
        const val ARQUIVO = "visura_sessao"
        const val PREFIXO = "v1:"
        const val ACCESS = "access_token"
        const val REFRESH = "refresh_token"
        const val NOME = "usuario_nome"
        const val EMAIL = "usuario_email"
        const val CPF = "usuario_cpf"
        const val DISPOSITIVO = "dispositivo_id"
    }
}
