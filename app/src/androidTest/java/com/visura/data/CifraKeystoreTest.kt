package com.visura.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

// Roda no aparelho porque usa o Android Keystore de verdade.
@RunWith(AndroidJUnit4::class)
class CifraKeystoreTest {

    private val cifra = CifraKeystore(alias = "visura_teste_cifra")

    @After
    fun apagarChave() = cifra.apagarChave()

    @Test
    fun decifraOQueCifrou() {
        val texto = "eyJhbGciOiJIUzI1NiJ9.token-de-teste.ção"
        assertEquals(texto, cifra.decifrar(cifra.cifrar(texto)))
    }

    @Test
    fun mesmoTextoGeraCifrasDiferentesENaoExpoeOTexto() {
        val a = cifra.cifrar("refresh-token")
        val b = cifra.cifrar("refresh-token")
        assertNotEquals(a, b) // IV aleatório a cada cifragem
        assertFalse(a.contains("refresh-token"))
    }

    @Test
    fun recusaValorAdulterado() {
        val cifrado = cifra.cifrar("access-token")
        val meio = cifrado.length / 2
        val trocado = if (cifrado[meio] == 'A') 'B' else 'A'
        val adulterado = cifrado.substring(0, meio) + trocado + cifrado.substring(meio + 1)
        assertThrows(Exception::class.java) { cifra.decifrar(adulterado) }
    }

    @Test
    fun semAChaveNaoDecifra() {
        val cifrado = cifra.cifrar("access-token")
        cifra.apagarChave()
        assertThrows(Exception::class.java) { cifra.decifrar(cifrado) }
    }
}
