package com.visura.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FormatacaoTest {

    @Test
    fun corAntigaSaiDaPrimeiraLinhaDasObservacoes() {
        assertEquals("Bege" to "Risco no canto", separarCor("Cor: Bege\nRisco no canto"))
        assertEquals("Branco" to "", separarCor("Cor: Branco"))
        assertEquals("" to "Sem cor", separarCor("Sem cor"))
    }

    @Test
    fun dataEHoraNoFusoDoAparelho() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"))
            assertEquals("18/07/2026 10:45", formatarDataHora("2026-07-18T13:45:00.123456Z"))
            assertEquals("", formatarDataHora(null))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun dataDoInicioDaNoiteNaoPulaParaOOutroDia() {
        val brasilia = TimeZone.getTimeZone("America/Sao_Paulo")
        // 01:30 em UTC ainda é 22:30 do dia anterior em Brasília
        assertEquals("18 jul 2026", formatarData("2026-07-19T01:30:00Z", brasilia))
        assertEquals("", formatarData("2026-07-19", brasilia))
    }

    @Test
    fun documentos() {
        assertEquals("529.982.247-25", formatarCpf("52998224725"))
        assertEquals("12.345.678/0001-95", formatarCnpj("12345678000195"))
    }
}
