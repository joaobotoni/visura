package com.visura.model

import java.util.UUID

// valorApi é o "condition" do item na API
enum class EstadoItem(val rotulo: String) {
    NOVO("Novo"),
    EXCELENTE("Excelente"),
    BOM("Bom"),
    REGULAR("Regular"),
    RUIM("Ruim"),
    DANIFICADO("Danificado");

    val valorApi: String get() = name.lowercase()
}

enum class StatusAmbiente { PENDENTE, EM_ANDAMENTO, CONCLUIDO }

data class ItemAmbiente(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val estado: EstadoItem? = null, // null = ainda não avaliado
    val descricao: String = "",
    val material: String = "",
    val cor: String = "",
    val observacoes: String = "",
    val fotos: Int = 0,
    val fotosIds: List<String> = emptyList() // ids das evidências na API, usados no laudo em PDF
)

// Narração em áudio do ambiente, já recebida pela API
data class Narracao(val id: String, val recebidaEm: String, val bytes: Int)

data class Ambiente(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val itens: List<ItemAmbiente>,
    val observacoes: String = "",
    val narracoes: List<Narracao> = emptyList()
) {
    val itensAvaliados: Int get() = itens.count { it.estado != null }

    val status: StatusAmbiente
        get() = when {
            itens.isNotEmpty() && itensAvaliados == itens.size -> StatusAmbiente.CONCLUIDO
            itensAvaliados > 0 -> StatusAmbiente.EM_ANDAMENTO
            else -> StatusAmbiente.PENDENTE
        }
}

data class TipoDeAmbiente(val id: String, val nome: String)

// Funções para alterar a lista de ambientes sem mutá-la (o ViewModel guarda a lista no estado)
fun List<Ambiente>.comItem(ambienteId: String, item: ItemAmbiente): List<Ambiente> = map { amb ->
    if (amb.id != ambienteId) amb
    else if (amb.itens.any { it.id == item.id }) amb.copy(itens = amb.itens.map { if (it.id == item.id) item else it })
    else amb.copy(itens = amb.itens + item)
}

fun List<Ambiente>.semItem(ambienteId: String, itemId: String): List<Ambiente> = map { amb ->
    if (amb.id == ambienteId) amb.copy(itens = amb.itens.filterNot { it.id == itemId }) else amb
}

// Itens que todo ambiente novo já traz (sem avaliação)
fun itensPadrao(): List<ItemAmbiente> =
    listOf("Piso", "Paredes", "Teto", "Iluminação", "Tomadas").map { ItemAmbiente(nome = it) }
