package com.visura.data

import com.visura.data.remote.ViaCepApi
import com.visura.data.remote.ViaCepEndereco
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuscaDeEnderecoTest {

    private val paulista = ViaCepEndereco("01310-100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP", null)

    private class ViaCepFalso(
        val porCep: Map<String, ViaCepEndereco>,
        val porRua: List<ViaCepEndereco> = emptyList()
    ) : ViaCepApi {
        var consulta: Triple<String, String, String>? = null

        override suspend fun porCep(cep: String) = porCep[cep] ?: ViaCepEndereco(null, null, null, null, null, "true")

        override suspend fun porLogradouro(uf: String, cidade: String, logradouro: String): List<ViaCepEndereco> {
            consulta = Triple(uf, cidade, logradouro)
            return porRua
        }
    }

    @Test
    fun cepComMascaraBuscaPeloCep() = runBlocking {
        val busca = BuscaDeEndereco(ViaCepFalso(mapOf("01310100" to paulista)))

        val resultado = busca.buscar("01310-100")

        assertEquals(1, resultado.size)
        assertEquals("Avenida Paulista", resultado[0].rua)
        assertEquals("", resultado[0].numero)
    }

    @Test
    fun cepInexistenteNaoTrazResultado() = runBlocking {
        assertTrue(BuscaDeEndereco(ViaCepFalso(emptyMap())).buscar("99999999").isEmpty())
        assertNull(BuscaDeEndereco(ViaCepFalso(emptyMap())).porCep("99999-999"))
    }

    @Test
    fun ruaCidadeUfBuscaPeloLogradouro() = runBlocking {
        val viaCep = ViaCepFalso(emptyMap(), listOf(paulista))

        val resultado = BuscaDeEndereco(viaCep).buscar("Avenida Paulista, São Paulo, sp")

        assertEquals(Triple("SP", "São Paulo", "Avenida Paulista"), viaCep.consulta)
        assertEquals("Bela Vista", resultado.single().bairro)
    }

    @Test(expected = ApiErro::class)
    fun buscaSemUfEhRecusada(): Unit = runBlocking {
        BuscaDeEndereco(ViaCepFalso(emptyMap())).buscar("Avenida Paulista, São Paulo")
    }
}
