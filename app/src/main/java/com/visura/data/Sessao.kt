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
// O identificador do dispositivo é gerado uma vez e reaproveitado em todo login (a API abre uma sessão por dispositivo).
class Sessao(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("visura_sessao", Context.MODE_PRIVATE)

    private val _expirou = MutableStateFlow(false)

    // Vira true quando o refresh token é recusado; o NavGraph manda o usuário de volta ao login.
    val expirou: StateFlow<Boolean> = _expirou.asStateFlow()

    val accessToken: String? get() = prefs.getString(ACCESS, null)
    val refreshToken: String? get() = prefs.getString(REFRESH, null)
    val logado: Boolean get() = refreshToken != null

    val usuario: UsuarioLogado?
        get() {
            val nome = prefs.getString(NOME, null) ?: return null
            return UsuarioLogado(nome, prefs.getString(EMAIL, "").orEmpty())
        }

    // A API não devolve o CPF do usuário; guardamos o que ele digitou no cadastro para exibir no perfil.
    var cpf: String?
        get() = prefs.getString(CPF, null)
        set(valor) = prefs.edit { putString(CPF, valor) }

    val dispositivo: String
        get() = prefs.getString(DISPOSITIVO, null) ?: UUID.randomUUID().toString().also { novo ->
            prefs.edit { putString(DISPOSITIVO, novo) }
        }

    fun salvarTokens(access: String, refresh: String) {
        prefs.edit {
            putString(ACCESS, access)
            putString(REFRESH, refresh)
        }
        _expirou.value = false
    }

    fun salvarUsuario(nome: String, email: String) {
        prefs.edit {
            putString(NOME, nome)
            putString(EMAIL, email)
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

    private companion object {
        const val ACCESS = "access_token"
        const val REFRESH = "refresh_token"
        const val NOME = "usuario_nome"
        const val EMAIL = "usuario_email"
        const val CPF = "usuario_cpf"
        const val DISPOSITIVO = "dispositivo_id"
    }
}
