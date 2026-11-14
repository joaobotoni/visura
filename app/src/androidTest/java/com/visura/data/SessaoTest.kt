package com.visura.data

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// A sessão de verdade, com o SharedPreferences e o Android Keystore do aparelho
// (arquivo e chave próprios do teste, para não mexer na sessão do app).
@RunWith(AndroidJUnit4::class)
class SessaoTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cifra = CifraKeystore(alias = "visura_teste_sessao")
    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)
    private lateinit var sessao: Sessao

    @Before
    fun criar() {
        prefs.edit(commit = true) { clear() }
        sessao = Sessao(context, cifra, ARQUIVO)
    }

    @After
    fun limpar() {
        prefs.edit(commit = true) { clear() }
        cifra.apagarChave()
    }

    @Test
    fun guardaTokensEDadosCifrados() {
        sessao.salvarTokens("access-123", "refresh-456")
        sessao.salvarUsuario("Maria Silva", "maria@exemplo.com")
        sessao.cpf = "52998224725"

        assertEquals("access-123", sessao.accessToken)
        assertEquals("refresh-456", sessao.refreshToken)
        assertEquals(UsuarioLogado("Maria Silva", "maria@exemplo.com"), sessao.usuario)
        assertEquals("52998224725", sessao.cpf)
        assertTrue(sessao.logado)

        // Nada disso aparece em texto puro no arquivo do SharedPreferences
        val gravado = prefs.all.values.joinToString(" ")
        listOf("access-123", "refresh-456", "Maria Silva", "maria@exemplo.com", "52998224725").forEach {
            assertFalse("$it gravado em texto puro", gravado.contains(it))
        }
    }

    @Test
    fun migraValoresAntigosEmTextoPuro() {
        prefs.edit(commit = true) {
            putString("access_token", "access-antigo")
            putString("refresh_token", "refresh-antigo")
        }

        assertEquals("refresh-antigo", sessao.refreshToken)
        assertEquals("access-antigo", sessao.accessToken)
        // Depois da primeira leitura, o arquivo já guarda o valor cifrado
        assertFalse(prefs.getString("refresh_token", null)!!.contains("refresh-antigo"))
        assertEquals("refresh-antigo", Sessao(context, cifra, ARQUIVO).refreshToken)
    }

    @Test
    fun chavePerdidaDescartaASessaoSemFecharOApp() {
        sessao.salvarTokens("access-123", "refresh-456")
        cifra.apagarChave()

        assertNull(sessao.refreshToken)
        assertFalse(sessao.logado)
        assertNull(prefs.getString("refresh_token", null))
    }

    @Test
    fun limparApagaASessaoEMantemODispositivo() {
        val dispositivo = sessao.dispositivo
        sessao.salvarTokens("access-123", "refresh-456")
        sessao.salvarUsuario("Maria Silva", "maria@exemplo.com")

        sessao.limpar(expirou = true)

        assertFalse(sessao.logado)
        assertNull(sessao.usuario)
        assertTrue(sessao.expirou.value)
        assertEquals(dispositivo, sessao.dispositivo)
    }

    private companion object {
        const val ARQUIVO = "visura_sessao_teste"
    }
}
