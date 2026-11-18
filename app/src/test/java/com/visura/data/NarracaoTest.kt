package com.visura.data

import com.visura.ui.screens.ambientes.cronometro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NarracaoTest {

    @Test
    fun semRespostaOuErroDoServidorTentaDeNovo() {
        assertTrue(temporario(ApiErro("sem rede")))
        assertTrue(temporario(ApiErro("sessão", 401)))
        assertTrue(temporario(ApiErro("limite", 429)))
        assertTrue(temporario(ApiErro("servidor", 503)))
    }

    @Test
    fun recusaDaApiNaoTentaDeNovo() {
        assertFalse(temporario(ApiErro("vistoria finalizada", 409)))
        assertFalse(temporario(ApiErro("ambiente removido", 404)))
        assertFalse(temporario(ApiErro("formato", 415)))
        assertFalse(temporario(ApiErro("regra", 422)))
    }

    @Test
    fun cronometroEmMinutosEHoras() {
        assertEquals("00:00", cronometro(0))
        assertEquals("01:05", cronometro(65_400))
        assertEquals("59:59", cronometro(3_599_000))
        assertEquals("1:00:07", cronometro(3_607_000))
    }
}
