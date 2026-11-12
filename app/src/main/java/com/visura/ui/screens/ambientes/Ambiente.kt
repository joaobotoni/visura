package com.visura.ui.screens.ambientes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
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
    val fotos: Int = 0 // SIMULAÇÃO — por enquanto só conta as fotos; a câmera ainda não existe
)

data class Ambiente(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val itens: List<ItemAmbiente>,
    val observacoes: String = ""
) {
    val itensAvaliados: Int get() = itens.count { it.estado != null }

    val status: StatusAmbiente
        get() = when {
            itens.isNotEmpty() && itensAvaliados == itens.size -> StatusAmbiente.CONCLUIDO
            itensAvaliados > 0 -> StatusAmbiente.EM_ANDAMENTO
            else -> StatusAmbiente.PENDENTE
        }
}

// Funções para alterar a lista de ambientes sem mutá-la (o NavGraph guarda a lista no estado)
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

private val iconesDeAmbiente = listOf(
    "quarto" to Icons.Filled.Bed,
    "suíte" to Icons.Filled.Bed,
    "sala" to Icons.Filled.Weekend,
    "cozinha" to Icons.Filled.Kitchen,
    "banheiro" to Icons.Filled.Bathtub,
    "lavabo" to Icons.Filled.Bathtub,
    "garagem" to Icons.Filled.Garage,
    "lavanderia" to Icons.Filled.LocalLaundryService,
    "área externa" to Icons.Filled.Yard,
    "escritório" to Icons.Filled.Work,
    "varanda" to Icons.Filled.DoorSliding,
    "closet" to Icons.Filled.Checkroom,
    "escada" to Icons.Filled.Stairs,
    "churrasqueira" to Icons.Filled.OutdoorGrill,
    "adega" to Icons.Filled.WineBar,
    "home theater" to Icons.Filled.Theaters,
    "piscina" to Icons.Filled.Pool,
    "quintal" to Icons.Filled.Deck,
    "depósito" to Icons.Filled.HomeRepairService,
    "hall" to Icons.Filled.MeetingRoom,
    "jardim" to Icons.Filled.Grass
)

private val iconesDeItem = listOf(
    "piso" to Icons.Filled.Layers,
    "parede" to Icons.Filled.Wallpaper,
    "pintura" to Icons.Filled.FormatPaint,
    "porta" to Icons.Filled.DoorFront,
    "janela" to Icons.Filled.Window,
    "tomada" to Icons.Filled.ElectricalServices,
    "iluminação" to Icons.Filled.Lightbulb,
    "lâmpada" to Icons.Filled.Lightbulb,
    "teto" to Icons.Filled.Roofing,
    "pia" to Icons.Filled.Countertops,
    "bancada" to Icons.Filled.Countertops,
    "chuveiro" to Icons.Filled.Shower,
    "armário" to Icons.Filled.Kitchen
)

// null quando o nome não bate com nenhum ícone conhecido (a tela usa um ícone genérico)
fun iconeDoAmbiente(nome: String): ImageVector? =
    iconesDeAmbiente.firstOrNull { nome.contains(it.first, ignoreCase = true) }?.second

fun iconeDoItem(nome: String): ImageVector? =
    iconesDeItem.firstOrNull { nome.contains(it.first, ignoreCase = true) }?.second
