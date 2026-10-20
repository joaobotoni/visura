package com.visura.ui.screens.ambientes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

enum class EstadoItem(val rotulo: String) {
    NOVO("Novo"),
    EXCELENTE("Excelente"),
    BOM("Bom"),
    REGULAR("Regular"),
    RUIM("Ruim"),
    DANIFICADO("Danificado")
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

// Tipos sugeridos na tela "Novo ambiente"
val tiposDeAmbienteComuns = listOf(
    "Escritório", "Varanda", "Closet", "Escada",
    "Churrasqueira", "Adega", "Home theater", "Piscina",
    "Quintal", "Depósito", "Hall de entrada", "Jardim"
)

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

// Mude para true para começar com todos os itens avaliados (atalho para testar a Revisão)
private const val SIMULAR_TUDO_AVALIADO = false

private fun avaliados(vararg itens: Pair<String, EstadoItem>, fotos: Int = 1) =
    itens.map { (nome, estado) -> ItemAmbiente(nome = nome, estado = estado, fotos = fotos) }

private fun pendentes(vararg nomes: String) = nomes.map { ItemAmbiente(nome = it) }

// Ambientes de exemplo de uma vistoria em andamento (3 de 7 avaliados, como no design)
private val ambientesDeExemploBase: List<Ambiente>
    get() = listOf(
        Ambiente(
            nome = "Sala",
            itens = avaliados(
                "Piso" to EstadoItem.BOM,
                "Paredes" to EstadoItem.EXCELENTE,
                "Pintura" to EstadoItem.BOM
            ) + ItemAmbiente(
                nome = "Portas",
                estado = EstadoItem.DANIFICADO,
                material = "Madeira",
                cor = "Marrom escuro",
                observacoes = "Porta com rachadura na parte inferior e dobradiça solta. Necessário reparo antes da próxima locação.",
                fotos = 2
            ) + avaliados(
                "Janelas" to EstadoItem.BOM,
                "Tomadas" to EstadoItem.BOM,
                "Iluminação" to EstadoItem.EXCELENTE,
                "Teto" to EstadoItem.BOM
            )
        ),
        Ambiente(
            nome = "Cozinha",
            itens = avaliados(
                "Piso" to EstadoItem.BOM,
                "Paredes" to EstadoItem.BOM,
                "Pia" to EstadoItem.EXCELENTE,
                "Armários" to EstadoItem.REGULAR,
                "Tomadas" to EstadoItem.BOM,
                "Iluminação" to EstadoItem.BOM
            )
        ),
        Ambiente(
            nome = "Quarto 1",
            itens = avaliados(
                "Piso" to EstadoItem.BOM,
                "Paredes" to EstadoItem.BOM,
                "Janelas" to EstadoItem.EXCELENTE,
                "Portas" to EstadoItem.BOM
            ) + pendentes("Tomadas", "Iluminação", "Teto")
        ),
        Ambiente(nome = "Banheiro", itens = pendentes("Piso", "Paredes", "Pia", "Chuveiro", "Iluminação")),
        Ambiente(nome = "Área externa", itens = pendentes("Piso", "Muros", "Portão", "Iluminação")),
        Ambiente(nome = "Garagem", itens = pendentes("Piso", "Portão", "Iluminação")),
        Ambiente(nome = "Lavanderia", itens = pendentes("Piso", "Tanque", "Tomadas", "Iluminação"))
    )

// Cada chamada gera ids novos, então toda vistoria começa do zero
fun ambientesDeExemplo(): List<Ambiente> =
    if (!SIMULAR_TUDO_AVALIADO) ambientesDeExemploBase
    else ambientesDeExemploBase.map { amb ->
        amb.copy(itens = amb.itens.map { if (it.estado == null) it.copy(estado = EstadoItem.BOM, fotos = 1) else it })
    }
