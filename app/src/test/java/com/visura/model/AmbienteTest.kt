package com.visura.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AmbienteTest {

    private val piso = ItemAmbiente(id = "piso", nome = "Piso")
    private val teto = ItemAmbiente(id = "teto", nome = "Teto")
    private val sala = Ambiente(id = "sala", nome = "Sala", itens = listOf(piso, teto))
    private val cozinha = Ambiente(id = "cozinha", nome = "Cozinha", itens = emptyList())

    @Test
    fun statusSegueOsItensAvaliados() {
        assertEquals(StatusAmbiente.PENDENTE, sala.status)
        assertEquals(StatusAmbiente.PENDENTE, cozinha.status) // sem itens não conta como concluído

        val metade = sala.copy(itens = listOf(piso.copy(estado = EstadoItem.BOM), teto))
        assertEquals(StatusAmbiente.EM_ANDAMENTO, metade.status)
        assertEquals(1, metade.itensAvaliados)

        val tudo = sala.copy(itens = sala.itens.map { it.copy(estado = EstadoItem.RUIM) })
        assertEquals(StatusAmbiente.CONCLUIDO, tudo.status)
    }

    @Test
    fun comItemTrocaOItemExistenteOuAcrescentaNoAmbienteCerto() {
        val lista = listOf(sala, cozinha)

        val avaliado = lista.comItem("sala", piso.copy(estado = EstadoItem.NOVO))
        assertEquals(listOf(EstadoItem.NOVO, null), avaliado[0].itens.map { it.estado })
        assertEquals(cozinha, avaliado[1])

        val pia = ItemAmbiente(id = "pia", nome = "Pia")
        val comPia = lista.comItem("cozinha", pia)
        assertEquals(listOf(pia), comPia[1].itens)
        assertEquals(sala, comPia[0])
    }

    @Test
    fun semItemRemoveSoDoAmbienteIndicado() {
        val lista = listOf(sala, cozinha.copy(itens = listOf(piso)))

        val resultado = lista.semItem("sala", "piso")

        assertEquals(listOf(teto), resultado[0].itens)
        assertEquals(listOf(piso), resultado[1].itens)
    }

    @Test
    fun valorDaApiEhONomeEmMinusculas() {
        assertEquals("danificado", EstadoItem.DANIFICADO.valorApi)
    }
}
